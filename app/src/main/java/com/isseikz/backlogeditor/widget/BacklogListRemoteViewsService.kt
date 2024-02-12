package com.isseikz.backlogeditor.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViewsService
import com.isseikz.backlogeditor.data.BacklogStatus
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
        val filterStatus = intent.getCharSequenceArrayExtra(
            EXTRA_FILTER_STATUS
        )?.map { BacklogStatus.valueOf(it.toString()) }?.toSet() ?: emptySet()
        Timber.d("onGetViewFactory $projectId ${this.hashCode()}")
        return BacklogListRemoteViewsFactory(
            this.applicationContext,
            backlogRepository,
            projectId,
            filterStatus
        )
    }

    companion object {
        const val EXTRA_PROJECT_ID = "project_id"
        const val EXTRA_FILTER_STATUS = "filter_status"
        fun createIntent(
            context: Context,
            projectId: String,
            filterStatus: Set<BacklogStatus>
        ): Intent {
            return Intent(context, BacklogListRemoteViewsService::class.java).apply {
                putExtra(EXTRA_PROJECT_ID, projectId)
                putExtra(
                    EXTRA_FILTER_STATUS,
                    filterStatus.map { it.name }.toTypedArray()
                )
                this.identifier = "$projectId:$filterStatus"
                data = Uri.fromParts("content", projectId, null)
            }
        }
    }
}
