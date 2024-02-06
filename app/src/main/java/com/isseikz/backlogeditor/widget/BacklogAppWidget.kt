package com.isseikz.backlogeditor.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.RemoteViews
import androidx.work.WorkManager
import com.isseikz.backlogeditor.R
import com.isseikz.backlogeditor.RefreshItemsReceiver
import com.isseikz.backlogeditor.SyncDataWorker
import com.isseikz.backlogeditor.source.BacklogRepository
import com.isseikz.backlogeditor.store.WidgetProjectRepository
import com.isseikz.backlogeditor.ui.AddItemDialogActivity
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
        if (System.currentTimeMillis() - backlogRepository.lastUpdated > 60_000L) {
            // when the app is installed (`lastUpdated = 0L`), or 60sec after the last sync
            SyncDataWorker.requestOneTimeSync(context)
        }
        updateWidget(context, appWidgetManager, appWidgetIds)
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        Timber.d("onAppWidgetOptionsChanged $appWidgetId, $newOptions")
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        widgetProjectRepository.widgetProjectMapFlow.value[appWidgetId]?.let {
            updateSingleWidget(context, appWidgetManager, appWidgetId, it)
        }
    }

    override fun onEnabled(context: Context) {
        Timber.d("onEnabled")
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
        widgetProjectRepository.widgetProjectMapFlow.value.let {
            it.filterKeys { widgetId -> appWidgetIds.contains(widgetId) }
                .forEach { (widgetId, projectId) ->
                    updateSingleWidget(context, appWidgetManager, widgetId, projectId)
                }
        }
    }

    private fun updateSingleWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        projectId: String
    ) {
        Timber.d("updateSingleWidget $projectId")

        val project = backlogRepository.getProjectInfo(projectId) ?: run {
            Timber.w("project is null with project $projectId")
            return
        }

        val intent = Intent(context, BacklogListRemoteViewsService::class.java).apply {
            putExtra(BacklogListRemoteViewsService.EXTRA_PROJECT_ID, project.projectId)
            data = Uri.fromParts("content", project.projectId, null)
        }

        val projectName = project.projectName
        Timber.d("[$projectId] ${project.items.size} projectName: $projectName")

        val views = RemoteViews(context.packageName, R.layout.backlog_widget).apply {
            setRemoteAdapter(R.id.listViewProjects, intent)
            setEmptyView(R.id.listViewProjects, R.id.empty_view)
            setTextViewText(R.id.backlog_widget_header_project, projectName)

            val addItemPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                AddItemDialogActivity.createIntent(context, projectId).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setOnClickPendingIntent(R.id.buttonAddItem, addItemPendingIntent)

            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                RefreshItemsReceiver.createIntent(context),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setOnClickPendingIntent(R.id.buttonRefreshItem, refreshPendingIntent)
        }
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        fun requestUpdate(applicationContext: Context) {
            Timber.d("requestUpdate")
            AppWidgetManager.getInstance(applicationContext).also { appWidgetManager ->
                val appWidgetIds = getWidgetIds(applicationContext)
                Timber.d("appWidgetIds: ${appWidgetIds.joinToString(", ")}")
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listViewProjects)
            }
        }

        private fun getWidgetIds(applicationContext: Context): IntArray {
            return AppWidgetManager.getInstance(applicationContext).getAppWidgetIds(
                ComponentName(applicationContext, BacklogAppWidget::class.java)
            )
        }
    }
}
