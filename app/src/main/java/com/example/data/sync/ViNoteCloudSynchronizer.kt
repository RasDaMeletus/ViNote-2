package com.example.data.sync

import android.util.Log
import com.example.data.auth.AuthRepository
import com.example.data.local.GoalDao
import com.example.data.local.SyncQueueDao
import com.example.data.local.TransactionDao
import com.example.data.local.entities.SyncOperation
import com.example.data.local.entities.SyncQueueStatus
import com.example.data.repository.FirestoreExpenseSyncRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class CloudSyncStatus {
    IDLE,
    SYNCING,
    SYNCED,
    OFFLINE,
    ERROR
}

data class SyncSummary(
    val status: CloudSyncStatus = CloudSyncStatus.IDLE,
    val pendingItemsCount: Int = 0,
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)

/**
 * Production Local-First Cloud Synchronizer.
 * Processes the Room SyncQueue, pushes local changes to Firestore / Next.js backend,
 * and pulls remote updates to ensure multi-device consistency.
 * Fault-tolerant: offline or network failures never block on-device operations.
 */
class ViNoteCloudSynchronizer(
    private val transactionDao: TransactionDao,
    private val goalDao: GoalDao,
    private val syncQueueDao: SyncQueueDao,
    private val authRepository: AuthRepository,
    private val firestoreRepository: FirestoreExpenseSyncRepository = FirestoreExpenseSyncRepository(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val _syncState = MutableStateFlow(SyncSummary())
    val syncState: StateFlow<SyncSummary> = _syncState.asStateFlow()

    init {
        // Observe pending queue size
        scope.launch {
            val userId = authRepository.getCanonicalUserId()
            syncQueueDao.getPendingCountFlow(userId).collect { count ->
                _syncState.value = _syncState.value.copy(pendingItemsCount = count)
            }
        }
    }

    suspend fun performFullSync(): SyncSummary = withContext(Dispatchers.IO) {
        val currentSession = authRepository.currentSession.value
        if (currentSession == null || !currentSession.isAuthenticated) {
            val idle = SyncSummary(status = CloudSyncStatus.OFFLINE, errorMessage = "Not signed in")
            _syncState.value = idle
            return@withContext idle
        }

        _syncState.value = _syncState.value.copy(status = CloudSyncStatus.SYNCING, errorMessage = null)
        Log.i("ViNoteSync", "Starting full cloud synchronization for user: ${currentSession.userId}")

        try {
            // 1. Drain pending queue
            val pendingQueue = syncQueueDao.getPendingQueue(currentSession.userId)
            for (queueItem in pendingQueue) {
                try {
                    syncQueueDao.updateStatus(queueItem.id, SyncQueueStatus.SYNCING)
                    when (queueItem.operation) {
                        SyncOperation.INSERT, SyncOperation.UPDATE -> {
                            if (queueItem.entityType == "TRANSACTION") {
                                val txId = queueItem.entityId.toLongOrNull()
                                if (txId != null) {
                                    val tx = transactionDao.getTransactionById(txId)
                                    if (tx != null) {
                                        firestoreRepository.syncLocalToRemote(listOf(tx))
                                    }
                                }
                            }
                        }
                        SyncOperation.DELETE -> {
                            if (queueItem.entityType == "TRANSACTION") {
                                val txId = queueItem.entityId.toLongOrNull()
                                if (txId != null) {
                                    firestoreRepository.deleteRemoteExpense(txId)
                                }
                            }
                        }
                    }
                    syncQueueDao.updateStatus(queueItem.id, SyncQueueStatus.SYNCED)
                } catch (e: Exception) {
                    Log.e("ViNoteSync", "Failed syncing queue item ${queueItem.id}", e)
                    syncQueueDao.markFailed(queueItem.id, SyncQueueStatus.FAILED, e.message)
                }
            }

            // 2. Perform two-way pull/merge
            val result = firestoreRepository.performFullSync(transactionDao)

            val summary = SyncSummary(
                status = if (result.isSuccess) CloudSyncStatus.SYNCED else CloudSyncStatus.ERROR,
                pendingItemsCount = syncQueueDao.getPendingQueue(currentSession.userId).size,
                lastSyncTimestamp = System.currentTimeMillis(),
                errorMessage = result.errorMessage
            )
            _syncState.value = summary
            Log.i("ViNoteSync", "Sync completed: isSuccess=${result.isSuccess}, uploaded=${result.uploadedCount}, downloaded=${result.downloadedCount}")
            summary
        } catch (e: Exception) {
            Log.e("ViNoteSync", "Sync execution error", e)
            val errorSummary = SyncSummary(
                status = CloudSyncStatus.ERROR,
                pendingItemsCount = _syncState.value.pendingItemsCount,
                lastSyncTimestamp = System.currentTimeMillis(),
                errorMessage = e.message
            )
            _syncState.value = errorSummary
            errorSummary
        }
    }
}
