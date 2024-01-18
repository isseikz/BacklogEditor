package com.isseikz.backlogeditor

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import com.isseikz.backlogeditor.ui.AddItemDialogActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class AddItemReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("onReceive")
        // Start an Activity to show the dialog
        val widgetId = intent.getIntExtra(
            android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID,
            android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
        )

        val dialogIntent = Intent(context, AddItemDialogActivity::class.java).apply {
            // Add this flag to start a new task because you're starting an Activity from a context that's not an Activity
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(dialogIntent)
    }

    companion object {
        private const val ACTION_ADD_ITEM = "com.isseikz.backlogeditor.action.ADD_ITEM"
        fun createIntent(context: Context, widgetId: Int): Intent {
            return Intent(context, AddItemReceiver::class.java).apply {
                action = ACTION_ADD_ITEM
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }
        }
    }
}
