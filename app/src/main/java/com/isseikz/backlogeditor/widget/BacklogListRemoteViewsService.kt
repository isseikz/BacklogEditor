package com.isseikz.backlogeditor.widget

import android.content.Intent
import android.widget.RemoteViewsService
import com.isseikz.backlogeditor.source.BacklogRepository
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class BacklogListRemoteViewsService : RemoteViewsService() {
    @Inject
    lateinit var backlogRepository: BacklogRepository

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val projectId = intent.getStringExtra(
            EXTRA_PROJECT_ID
        ) ?: "".also { Timber.w("projectId is null") }
        Timber.d("onGetViewFactory $projectId ${this.hashCode()}")
        return BacklogListRemoteViewsFactory(
            this.applicationContext,
            backlogRepository,
            projectId
        )
    }

    companion object {
        const val EXTRA_PROJECT_ID = "project_id"
        fun createIntent(projectId: String): Intent {
            return Intent().apply {
                putExtra(EXTRA_PROJECT_ID, projectId)
            }
        }
    }
}
