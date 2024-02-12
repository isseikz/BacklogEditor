package com.isseikz.backlogeditor.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import androidx.work.WorkManager
import com.isseikz.backlogeditor.R
import com.isseikz.backlogeditor.RefreshItemsReceiver
import com.isseikz.backlogeditor.data.WidgetProjectPreference
import com.isseikz.backlogeditor.source.BacklogRepository
import com.isseikz.backlogeditor.store.WidgetProjectRepository
import com.isseikz.backlogeditor.ui.AddItemDialogActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class BacklogAppWidget : AppWidgetProvider() {
    @Inject
    lateinit var widgetProjectRepository: WidgetProjectRepository

    @Inject
    lateinit var backlogRepository: BacklogRepository
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Timber.d("onUpdate")
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
            scope.launch { updateSingleWidget(context, appWidgetManager, it) }
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
        scope.launch {
            widgetProjectRepository.widgetProjectMapFlow.collect {
                it.filterKeys { widgetId -> appWidgetIds.contains(widgetId) }
                    .forEach { (_, preference) ->
                        updateSingleWidget(context, appWidgetManager, preference)
                    }
            }
        }
    }

    private suspend fun updateSingleWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        preference: WidgetProjectPreference
    ) = withContext(Dispatchers.IO) {
        Timber.d("updateSingleWidget ${preference.projectId}")
        backlogRepository.syncBacklogItems(preference.projectId)

        val project = backlogRepository.getProjectInfo(preference.projectId) ?: run {
            Timber.w("project is null with project ${preference.projectId}")
            return@withContext
        }

        val intent = BacklogListRemoteViewsService.createIntent(
            context,
            project.projectId,
            preference.statusFilter
        )
        val projectName = project.projectName
        Timber.d("[${preference.projectId}] ${project.items.size} projectName: $projectName")

        val views = RemoteViews(context.packageName, R.layout.backlog_widget).apply {
            setRemoteAdapter(R.id.listViewProjects, intent)
            setEmptyView(R.id.listViewProjects, R.id.empty_view)
            setTextViewText(R.id.backlog_widget_header_project, projectName)

            val addItemPendingIntent = PendingIntent.getActivity(
                context,
                preference.widgetId,
                AddItemDialogActivity.createIntent(context, preference.projectId).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setOnClickPendingIntent(R.id.buttonAddItem, addItemPendingIntent)

            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                preference.widgetId,
                RefreshItemsReceiver.createIntent(context),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setOnClickPendingIntent(R.id.buttonRefreshItem, refreshPendingIntent)
        }
        withContext(Dispatchers.Main) {
            appWidgetManager.updateAppWidget(preference.widgetId, views)
        }
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
