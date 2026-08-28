package com.example.services.wallet

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.domain.wallet.WalletNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Native Android NotificationListenerService for automatic real-time E-Wallet and Banking detection.
 * Captures notifications from GoPay, OVO, DANA, BCA, Mandiri, etc. and pipes them to WalletTransactionProcessor.
 */
class WalletNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName ?: return
        val extras = sbn.notification?.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()

        if (title.isBlank() && text.isBlank()) return

        val notificationObj = WalletNotification(
            packageName = packageName,
            title = title,
            text = text,
            bigText = bigText,
            timestamp = sbn.postTime,
            id = "${sbn.id}_${sbn.postTime}"
        )

        Log.d("WalletNotifListener", "Captured notification from $packageName: $title - $text")

        // Pass to global processor if active
        val processor = globalProcessor
        if (processor != null && isListenerActive) {
            CoroutineScope(Dispatchers.IO).launch {
                processor.processNotification(notificationObj)
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isServiceConnected = true
        Log.i("WalletNotifListener", "ViNote Notification Listener successfully connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isServiceConnected = false
        Log.i("WalletNotifListener", "ViNote Notification Listener disconnected")
    }

    companion object {
        var globalProcessor: WalletTransactionProcessor? = null
        var isListenerActive: Boolean = true
        var isServiceConnected: Boolean = false
    }
}
