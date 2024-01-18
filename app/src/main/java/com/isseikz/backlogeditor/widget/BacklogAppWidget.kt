package com.isseikz.backlogeditor.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.isseikz.backlogeditor.AddItemReceiver
import com.isseikz.backlogeditor.R
import com.isseikz.backlogeditor.SyncDataWorker
import com.isseikz.backlogeditor.source.BacklogRepository
import com.isseikz.backlogeditor.store.WidgetProjectRepository
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class BacklogAppWidget : AppWidgetProvider() {
    @Inject
    lateinit var widgetProjectRepository: WidgetProjectRepository
    @Inject
    lateinit var backlogRepository: BacklogRepository

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Timber.d("onUpdate")
        updateWidget(context, appWidgetManager, appWidgetIds)
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onEnabled(context: Context) {
        Timber.d("onEnabled")

        SyncDataWorker.requestOneTimeSync(context)
    }

    override fun onDisabled(context: Context) {
        Timber.d("onDisabled")
        WorkManager.getInstance(context).cancelAllWork()
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Timber.d("updateWidget ${appWidgetIds.joinToString(", ")}   ${this.hashCode()}")

        for (appWidgetId in appWidgetIds) {
            val project = widgetProjectRepository.widgetProjectMapFlow.value[appWidgetId]?.let {
                backlogRepository.getProjectInfo(it)
            } ?: run {
                Timber.w("project is null with appWidgetId $appWidgetId")
                return
            }

            val intent = Intent(context, BacklogListRemoteViewsService::class.java).apply {
                putExtra(BacklogListRemoteViewsService.BUNDLE_KEY_PROJECT_ID, project.projectId)
                data = Uri.fromParts("content", project.projectId, null)
            }

            val projectName =  project.projectName
            Timber.d("[$appWidgetId] ${project.items.size} projectName: $projectName")

            val views = RemoteViews(context.packageName, R.layout.backlog_widget).apply {
                setRemoteAdapter(R.id.listViewProjects, intent)
                setEmptyView(R.id.listViewProjects, R.id.empty_view)
                setTextViewText(R.id.backlog_widget_header_project, projectName)

                val addItemPendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    AddItemReceiver.createIntent(context, appWidgetId),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.buttonAddItem, addItemPendingIntent)
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    companion object {
        fun requestUpdate(applicationContext: Context) {
            Timber.d("requestUpdate")
            AppWidgetManager.getInstance(applicationContext).also { appWidgetManager ->
                val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    ComponentName(applicationContext, BacklogAppWidget::class.java)
                )
                Timber.d("appWidgetIds: ${appWidgetIds.joinToString(", ")}")
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listViewProjects)
            }
        }
    }
}
