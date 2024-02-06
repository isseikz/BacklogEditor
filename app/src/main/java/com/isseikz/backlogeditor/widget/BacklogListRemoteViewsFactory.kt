package com.isseikz.backlogeditor.widget

import android.content.Context
import android.widget.RemoteViews
import android.widget.RemoteViewsService.RemoteViewsFactory
import com.isseikz.backlogeditor.source.BacklogRepository
import com.isseikz.backlogeditor.R
import timber.log.Timber

class BacklogListRemoteViewsFactory(
    private val context: Context,
    private val backlogRepository: BacklogRepository,
    private val projectId: String
) : RemoteViewsFactory {
    private var projectItems = backlogRepository.getProjectInfo(projectId)?.items ?: emptyList()

    override fun onCreate() {
        Timber.d("onCreate (projectId: $projectId)")
    }

    override fun onDataSetChanged() {
        Timber.d("onDataSetChanged (projectId: $projectId)")
        projectItems = backlogRepository.getProjectInfo(projectId)?.items ?: emptyList()
        Timber.d("projectItems of ${projectItems.size}")
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        // Clear the data when the factory is destroyed
    }

    override fun getCount(): Int {
        Timber.d("getCount ${projectItems.size}")
        // Return the number of items in the list
        return projectItems.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        Timber.d("getViewAt of #$projectId $position -> ${projectItems[position].title}")
        return RemoteViews(context.packageName, R.layout.project_list_item).apply {
            setTextViewText(R.id.project_list_item_title, projectItems[position].title)
        }
    }

    override fun getLoadingView(): RemoteViews {
        Timber.d("getLoadingView")
        return RemoteViews(context.packageName, R.layout.widget_item_loading)
    }

    override fun getViewTypeCount(): Int {
        Timber.d("getViewTypeCount")
        // Return the number of different types of views
        return 1
    }

    override fun getItemId(position: Int): Long {
        Timber.d("getItemId $position")
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        Timber.d("hasStableIds")
        // Return whether the same ID always refers to the same object
        return true
    }

    data class ProjectItem(val name: String) // Placeholder data class
}
