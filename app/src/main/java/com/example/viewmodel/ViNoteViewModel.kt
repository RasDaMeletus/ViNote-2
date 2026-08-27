package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.engine.ExtractedReceiptData
import com.example.data.engine.ExtractedVoiceEntity
import com.example.data.engine.OfflineNlpEngine
import com.example.data.local.ViNoteDatabase
import com.example.data.model.Achievement
import com.example.data.model.ChatMessage
import com.example.data.model.ConnectedWallet
import com.example.data.model.GoalItem
import com.example.data.model.NotaAccessory
import com.example.data.model.NotaBaseColor
import com.example.data.model.NotaConfig
import com.example.data.model.NotaEyeState
import com.example.data.model.NotaPresenceMode
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionSource
import com.example.data.model.TransactionType
import com.example.data.repository.SyncResult
import com.example.data.repository.SyncStatus
import com.example.data.repository.ViNoteRepository
import com.example.ui.components.FormatUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ActivityFilter {
    ALL,
    INCOME,
    EXPENSE
}

class ViNoteViewModel(application: Application) : AndroidViewModel(application) {
    private val database = ViNoteDatabase.getDatabase(application)
    private val repository = ViNoteRepository(database.transactionDao(), database.goalDao())

    // Transactions Flow
    val allTransactions: StateFlow<List<TransactionItem>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Goals Flow
    val allGoals: StateFlow<List<GoalItem>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search and Filter for Activity screen
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _activityFilter = MutableStateFlow(ActivityFilter.ALL)
    val activityFilter = _activityFilter.asStateFlow()

    val filteredTransactions: StateFlow<List<TransactionItem>> = combine(
        allTransactions,
        searchQuery,
        activityFilter
    ) { transactions, query, filter ->
        transactions.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.category.contains(query, ignoreCase = true) ||
                    item.merchant.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                ActivityFilter.ALL -> true
                ActivityFilter.INCOME -> item.type == TransactionType.INCOME
                ActivityFilter.EXPENSE -> item.type == TransactionType.EXPENSE
            }
            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Connected Wallets
    private val _wallets = MutableStateFlow(
        listOf(
            ConnectedWallet("gopay", "GoPay", isConnected = true, isActiveSync = true, iconColorHex = "#00B14F", description = "Active Sync"),
            ConnectedWallet("ovo", "OVO", isConnected = true, isActiveSync = true, iconColorHex = "#4C3494", description = "Active Sync"),
            ConnectedWallet("dana", "DANA", isConnected = false, isActiveSync = false, iconColorHex = "#118EEA", description = "Ready to connect")
        )
    )
    val wallets = _wallets.asStateFlow()

    private val _isDetectionActive = MutableStateFlow(true)
    val isDetectionActive = _isDetectionActive.asStateFlow()

    // Nota Configuration State
    private val _notaConfig = MutableStateFlow(
        NotaConfig(
            baseColor = NotaBaseColor.SOFT_PINK,
            accessory = NotaAccessory.NONE,
            personalitySlider = 75f,
            presenceMode = NotaPresenceMode.ALWAYS_VISIBLE,
            offlineAiEngineDownloaded = true,
            eyeState = NotaEyeState.HAPPY
        )
    )
    val notaConfig = _notaConfig.asStateFlow()

    // Achievements
    val achievements = listOf(
        Achievement("1", "Smart Saver", "Saved 20% of monthly budget", true, "12 Unlocked"),
        Achievement("2", "Streak Master", "14 days tracking without missing", true, "14 Days"),
        Achievement("3", "Receipt Hunter", "Scanned 10 receipts", true, "Level 8")
    )

    // Chat with Nota
    private val _chatMessages = MutableStateFlow(
        listOf(
            ChatMessage(
                text = "Hey! What are we figuring out today?",
                isUser = false,
                quickChips = listOf("Can I afford this?", "Where did my money go?", "Help me save"),
                eyeState = NotaEyeState.CURIOUS
            )
        )
    )
    val chatMessages = _chatMessages.asStateFlow()

    private val _isNotaTyping = MutableStateFlow(false)
    val isNotaTyping = _isNotaTyping.asStateFlow()

    // Pending Transaction for Confirmation Sheet
    private val _pendingTransaction = MutableStateFlow<TransactionItem?>(null)
    val pendingTransaction = _pendingTransaction.asStateFlow()

    // Keypad Input for Add Transaction
    private val _keypadAmount = MutableStateFlow("0")
    val keypadAmount = _keypadAmount.asStateFlow()

    // Voice recognition live text & NLP extraction
    private val _voiceTranscript = MutableStateFlow("Makan siang nasi padang 35 ribu pakai GoPay")
    val voiceTranscript = _voiceTranscript.asStateFlow()

    private val _parsedVoiceEntity = MutableStateFlow<ExtractedVoiceEntity?>(
        OfflineNlpEngine.parseSpokenTransaction("Makan siang nasi padang 35 ribu pakai GoPay")
    )
    val parsedVoiceEntity = _parsedVoiceEntity.asStateFlow()

    private val _isVoiceListening = MutableStateFlow(false)
    val isVoiceListening = _isVoiceListening.asStateFlow()

    // Scan OCR status & extracted receipt
    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val _selectedReceiptPreset = MutableStateFlow("GrabFood Order")
    val selectedReceiptPreset = _selectedReceiptPreset.asStateFlow()

    private val _extractedReceiptData = MutableStateFlow<ExtractedReceiptData?>(
        OfflineNlpEngine.parseReceiptTextLines(OfflineNlpEngine.sampleReceipts["GrabFood Order"] ?: emptyList())
    )
    val extractedReceiptData = _extractedReceiptData.asStateFlow()

    // Notification banner state
    private val _bannerNotification = MutableStateFlow<String?>(null)
    val bannerNotification = _bannerNotification.asStateFlow()

    // Firestore Sync Status
    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus = _syncStatus.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow<Long?>(null)
    val lastSyncTimestamp = _lastSyncTimestamp.asStateFlow()

    // Selected Transaction for Detail / Delete Modal
    private val _selectedTransactionDetail = MutableStateFlow<TransactionItem?>(null)
    val selectedTransactionDetail = _selectedTransactionDetail.asStateFlow()

    // Computed Financial Balances
    val baseAvailableBalance: Long = 1250000L
    val safeMoney: Long = 750000L
    val mandatorySavings: Long = 500000L
    val dailyLimit: Long = 180000L

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setActivityFilter(filter: ActivityFilter) {
        _activityFilter.value = filter
    }

    fun toggleDetection(enabled: Boolean) {
        _isDetectionActive.value = enabled
    }

    fun toggleWalletSync(walletId: String) {
        _wallets.value = _wallets.value.map {
            if (it.id == walletId) it.copy(isActiveSync = !it.isActiveSync, isConnected = !it.isConnected) else it
        }
    }

    fun updateNotaBaseColor(color: NotaBaseColor) {
        _notaConfig.value = _notaConfig.value.copy(baseColor = color)
    }

    fun updateNotaAccessory(accessory: NotaAccessory) {
        _notaConfig.value = _notaConfig.value.copy(accessory = accessory)
    }

    fun updateNotaPersonality(sliderValue: Float) {
        val eye = when {
            sliderValue > 66f -> NotaEyeState.EXCITED
            sliderValue > 33f -> NotaEyeState.HAPPY
            else -> NotaEyeState.NEUTRAL
        }
        _notaConfig.value = _notaConfig.value.copy(
            personalitySlider = sliderValue,
            eyeState = eye
        )
    }

    fun updateNotaPresence(mode: NotaPresenceMode) {
        _notaConfig.value = _notaConfig.value.copy(presenceMode = mode)
    }

    fun toggleOfflineAi(enabled: Boolean) {
        _notaConfig.value = _notaConfig.value.copy(offlineAiEngineDownloaded = enabled)
        showBanner(if (enabled) "Offline On-Device AI Activated (No Cloud Latency)" else "Cloud Fallback Active")
    }

    // Keypad Logic
    fun appendKeypadDigit(digit: String) {
        if (_keypadAmount.value == "0" && digit != "000") {
            _keypadAmount.value = digit
        } else if (_keypadAmount.value != "0" && _keypadAmount.value.length < 10) {
            _keypadAmount.value += digit
        }
    }

    fun deleteKeypadDigit() {
        if (_keypadAmount.value.length > 1) {
            _keypadAmount.value = _keypadAmount.value.dropLast(1)
        } else {
            _keypadAmount.value = "0"
        }
    }

    fun clearKeypad() {
        _keypadAmount.value = "0"
    }

    fun preparePendingTransactionFromKeypad(category: String = "Food", title: String = "Expense") {
        val amount = _keypadAmount.value.toLongOrNull() ?: 0L
        if (amount > 0) {
            _pendingTransaction.value = TransactionItem(
                title = title,
                amount = amount,
                category = category,
                type = TransactionType.EXPENSE,
                merchant = title,
                source = TransactionSource.MANUAL,
                timeLabel = "Just now"
            )
        }
    }

    fun setPendingTransaction(transaction: TransactionItem?) {
        _pendingTransaction.value = transaction
    }

    fun confirmPendingTransaction() {
        _pendingTransaction.value?.let { tx ->
            viewModelScope.launch {
                repository.insertTransaction(tx)
                _pendingTransaction.value = null
                _keypadAmount.value = "0"
                showBanner("Payment recorded: ${FormatUtils.formatRupiah(tx.amount)} (${tx.category})")
            }
        }
    }

    fun dismissPendingTransaction() {
        _pendingTransaction.value = null
    }

    fun addManualTransaction(title: String, amount: Long, category: String, type: TransactionType = TransactionType.EXPENSE) {
        viewModelScope.launch {
            val item = TransactionItem(
                title = title,
                amount = amount,
                category = category,
                type = type,
                merchant = title,
                source = TransactionSource.MANUAL,
                timeLabel = "Just now"
            )
            repository.insertTransaction(item)
            showBanner("Transaction added!")
        }
    }

    fun selectTransactionDetail(transaction: TransactionItem?) {
        _selectedTransactionDetail.value = transaction
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
            if (_selectedTransactionDetail.value?.id == id) {
                _selectedTransactionDetail.value = null
            }
            showBanner("Transaction deleted")
        }
    }

    fun clearAllTransactions() {
        viewModelScope.launch {
            repository.clearAllTransactions()
            _selectedTransactionDetail.value = null
            showBanner("All transaction history deleted")
        }
    }

    fun syncExpensesWithFirestore() {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.SYNCING
            val result = repository.syncExpensesWithFirestore()
            if (result.isSuccess) {
                _syncStatus.value = SyncStatus.SUCCESS
                _lastSyncTimestamp.value = System.currentTimeMillis()
                val countMsg = if (result.uploadedCount > 0 || result.downloadedCount > 0) {
                    "Cloud sync complete (↑${result.uploadedCount} ↓${result.downloadedCount})"
                } else {
                    "Cloud sync complete (Up to date)"
                }
                showBanner(countMsg)
            } else {
                _syncStatus.value = SyncStatus.ERROR
                showBanner("Sync failed: ${result.errorMessage ?: "Network issue"}")
            }
        }
    }

    // Goals Logic
    fun createGoal(title: String, targetAmount: Long, targetDate: String = "In 3 months", category: String = "Savings") {
        viewModelScope.launch {
            val icons = listOf("headphones", "flight_takeoff", "school", "laptop", "directions_car", "home")
            val icon = when {
                title.contains("trip", true) || title.contains("holiday", true) || title.contains("liburan", true) -> "flight_takeoff"
                title.contains("school", true) || title.contains("kuliah", true) || title.contains("buku", true) -> "school"
                title.contains("headphone", true) || title.contains("audio", true) -> "headphones"
                else -> icons.random()
            }
            val newGoal = GoalItem(
                title = title,
                targetAmount = targetAmount,
                currentAmount = 0L,
                targetDateDescription = targetDate,
                category = category,
                iconName = icon
            )
            repository.insertGoal(newGoal)
            showBanner("Goal '${title}' created successfully! 🎉")
        }
    }

    fun addSavingsToGoal(goal: GoalItem, amount: Long) {
        viewModelScope.launch {
            val updated = goal.copy(currentAmount = (goal.currentAmount + amount).coerceAtMost(goal.targetAmount))
            repository.updateGoal(updated)
            showBanner("Saved ${FormatUtils.formatRupiah(amount)} to ${goal.title}!")
        }
    }

    fun deleteGoal(id: Long) {
        viewModelScope.launch {
            repository.deleteGoal(id)
            showBanner("Goal removed")
        }
    }

    // Voice Input & Offline NLP Processing
    fun setVoiceTranscript(text: String) {
        _voiceTranscript.value = text
        _parsedVoiceEntity.value = OfflineNlpEngine.parseSpokenTransaction(text)
    }

    fun simulateVoiceStreaming(utterance: String) {
        viewModelScope.launch {
            _isVoiceListening.value = true
            val words = utterance.split(" ")
            val sb = StringBuilder()
            for (word in words) {
                if (sb.isNotEmpty()) sb.append(" ")
                sb.append(word)
                _voiceTranscript.value = sb.toString()
                _parsedVoiceEntity.value = OfflineNlpEngine.parseSpokenTransaction(sb.toString())
                delay(120)
            }
            _isVoiceListening.value = false
        }
    }

    fun processVoiceInput() {
        val transcript = _voiceTranscript.value
        val entity = OfflineNlpEngine.parseSpokenTransaction(transcript)
        _parsedVoiceEntity.value = entity

        _pendingTransaction.value = TransactionItem(
            title = entity.title,
            amount = entity.amount,
            category = entity.category,
            type = entity.type,
            merchant = entity.merchant,
            source = TransactionSource.VOICE,
            timeLabel = "Just now"
        )
    }

    // Receipt Scan & Offline OCR Processing
    fun selectReceiptPreset(presetName: String) {
        _selectedReceiptPreset.value = presetName
        val lines = OfflineNlpEngine.sampleReceipts[presetName] ?: emptyList()
        _extractedReceiptData.value = OfflineNlpEngine.parseReceiptTextLines(lines)
    }

    fun startReceiptScanning(receiptName: String? = null, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isScanning.value = true
            delay(1200)

            val targetPreset = receiptName ?: _selectedReceiptPreset.value
            val lines = OfflineNlpEngine.sampleReceipts[targetPreset] ?: OfflineNlpEngine.sampleReceipts.values.first()
            val parsedData = OfflineNlpEngine.parseReceiptTextLines(lines)
            _extractedReceiptData.value = parsedData
            _isScanning.value = false

            _pendingTransaction.value = TransactionItem(
                title = parsedData.merchant,
                amount = parsedData.totalAmount,
                category = parsedData.category,
                type = TransactionType.EXPENSE,
                merchant = parsedData.merchant,
                source = TransactionSource.SCAN,
                timeLabel = "Just now"
            )
            onComplete()
        }
    }

    // Chat conversation
    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return

        val userMsg = ChatMessage(text = userText, isUser = true)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            _isNotaTyping.value = true
            delay(1200)

            val reply = generateNotaResponse(userText)
            _chatMessages.value = _chatMessages.value + reply
            _isNotaTyping.value = false
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                text = "Hey! What are we figuring out today?",
                isUser = false,
                quickChips = listOf("Can I afford this?", "Where did my money go?", "Help me save"),
                eyeState = NotaEyeState.CURIOUS
            )
        )
    }

    fun cycleNotaExpression() {
        val nextEye = when (_notaConfig.value.eyeState) {
            NotaEyeState.HAPPY -> NotaEyeState.CURIOUS
            NotaEyeState.CURIOUS -> NotaEyeState.EXCITED
            NotaEyeState.EXCITED -> NotaEyeState.PROUD
            NotaEyeState.PROUD -> NotaEyeState.THINKING
            NotaEyeState.THINKING -> NotaEyeState.NEUTRAL
            NotaEyeState.NEUTRAL -> NotaEyeState.HAPPY
        }
        _notaConfig.value = _notaConfig.value.copy(eyeState = nextEye)
    }

    private fun generateNotaResponse(query: String): ChatMessage {
        val lower = query.lowercase()
        val isPlayful = _notaConfig.value.personalitySlider > 50f

        return when {
            lower.contains("afford") || lower.contains("bisa beli") || lower.contains("cukup") || lower.contains("dinner") || lower.contains("makan malam") -> {
                ChatMessage(
                    text = if (isPlayful)
                        "You currently have Rp 750.000 in Uang Aman! ✨ If dinner is under Rp 150.000, you're 100% safe to go for it! 🍣"
                    else
                        "Based on your current balance of Rp 1.250.000 and safe spending threshold of Rp 750.000, discretionary spending under Rp 150.000 is fully within your daily limit.",
                    isUser = false,
                    quickChips = listOf("Check budget", "View goals", "Log expense"),
                    eyeState = NotaEyeState.HAPPY
                )
            }
            lower.contains("coffee") || lower.contains("kopi") -> {
                ChatMessage(
                    text = if (isPlayful)
                        "You spent Rp 45.000 on Kopi Kenangan this week! ☕ You're well within your coffee allowance. Treat yourself!"
                    else
                        "Coffee expenditures for the last 7 days total Rp 45.000, representing 4.3% of discretionary expenses.",
                    isUser = false,
                    quickChips = listOf("Set Coffee limit", "Where did my money go?"),
                    eyeState = NotaEyeState.EXCITED
                )
            }
            lower.contains("where did my money go") || lower.contains("kemana") || lower.contains("boros") || lower.contains("summary") || lower.contains("ringkasan") -> {
                ChatMessage(
                    text = if (isPlayful)
                        "I analyzed your spending! 🍔 58% went to Food (GrabFood & Kopi), and 24% to Transport. You're still on track for today's limit! 🚀"
                    else
                        "Top expense categories this week: Food (Rp 105.000) and Transportation (Rp 55.000). Total daily spending is currently at 42% of target limit.",
                    isUser = false,
                    quickChips = listOf("Set Food limit", "View Activity", "Save tips"),
                    eyeState = NotaEyeState.THINKING
                )
            }
            lower.contains("help me save") || lower.contains("tabung") || lower.contains("tips") || lower.contains("goal") -> {
                ChatMessage(
                    text = if (isPlayful)
                        "Yay! If you save Rp 15.000/day starting today, you'll reach your 'New Headphones' goal by next month! Shall we lock that in? 🎧✨"
                    else
                        "Recommended allocation: Divert Rp 25.000 daily to your 'Holiday' goal and maintain discretionary spending below Rp 60.000/day.",
                    isUser = false,
                    quickChips = listOf("Save Rp 15.000", "New Goal", "Check Safe Money"),
                    eyeState = NotaEyeState.PROUD
                )
            }
            lower.contains("safe money") || lower.contains("uang aman") || lower.contains("saldo") -> {
                ChatMessage(
                    text = if (isPlayful)
                        "Your Uang Aman is Rp 750.000! 🛡️ This is guilt-free spending money after all bills and savings are safely locked away."
                    else
                        "Your current safe spending limit is Rp 750.000, separated from reserved savings.",
                    isUser = false,
                    quickChips = listOf("Can I afford dinner?", "Help me save"),
                    eyeState = NotaEyeState.HAPPY
                )
            }
            else -> {
                ChatMessage(
                    text = if (isPlayful)
                        "I'm keeping a close eye on all your transactions! Everything looks smooth today ✨ Ask me anything about your budget or wallets."
                    else
                        "All connected services (GoPay, OVO) are actively synchronized. Your financial health index is optimal at 80% (Level 8).",
                    isUser = false,
                    quickChips = listOf("Can I afford this?", "Where did my money go?", "Help me save"),
                    eyeState = NotaEyeState.CURIOUS
                )
            }
        }
    }

    private fun showBanner(message: String) {
        viewModelScope.launch {
            _bannerNotification.value = message
            delay(3000)
            if (_bannerNotification.value == message) {
                _bannerNotification.value = null
            }
        }
    }
}
