package com.isseikz.backlogeditor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.isseikz.backlogeditor.widget.BacklogAppWidget

class RefreshItemsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_REFRESH_ITEMS) {
            throw IllegalStateException("This receiver only accepts $ACTION_REFRESH_ITEMS")
        }

        BacklogAppWidget.requestUpdate(context)
        SyncDataWorker.requestOneTimeSync(context)
    }

    companion object {
        private const val ACTION_REFRESH_ITEMS = "com.isseikz.backlogeditor.action.REFRESH_ITEMS"
        fun createIntent(context: Context): Intent {
            return Intent(context, RefreshItemsReceiver::class.java).apply {
                action = ACTION_REFRESH_ITEMS
            }
        }
    }
}
