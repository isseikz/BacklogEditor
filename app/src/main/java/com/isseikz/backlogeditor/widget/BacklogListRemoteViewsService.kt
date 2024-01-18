package com.isseikz.backlogeditor.widget

import android.content.Intent
import android.os.IBinder
import android.widget.RemoteViewsService
import com.isseikz.backlogeditor.source.BacklogRepository
import com.isseikz.backlogeditor.store.WidgetProjectRepository
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class BacklogListRemoteViewsService : RemoteViewsService() {
    @Inject
    lateinit var backlogRepository: BacklogRepository

//    override fun onBind(intent: Intent?): IBinder? {
//        Timber.d("onBind ${this.hashCode()}")
//        intent?.let {
//            Timber.d("projectId: ${it.getStringExtra(BUNDLE_KEY_PROJECT_ID)} ${it.data}")
//        }
//        return super.onBind(intent)
//    }

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val projectId = intent.getStringExtra(
            BUNDLE_KEY_PROJECT_ID
        ) ?: "".also { Timber.w("projectId is null") }
        Timber.d("onGetViewFactory $projectId ${this.hashCode()}")
        return BacklogListRemoteViewsFactory(
            this.applicationContext,
            backlogRepository,
            projectId
        )
    }

    companion object {
        const val BUNDLE_KEY_PROJECT_ID = "project_id"
    }
}
