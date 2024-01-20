package com.isseikz.backlogeditor

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.isseikz.backlogeditor.source.BacklogRepository
import com.isseikz.backlogeditor.widget.BacklogAppWidget
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class SyncDataWorker @AssistedInject constructor(
    @Assisted applicationContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val backlogRepository: BacklogRepository,
) : CoroutineWorker(applicationContext, workerParams) {
    override suspend fun doWork(): Result {

        backlogRepository.syncBacklogItems().also {
            Timber.d("hashcode: ${backlogRepository.hashCode()}")
        }

        BacklogAppWidget.requestUpdate(applicationContext)
        return Result.success()
    }

    companion object {
        fun requestOneTimeSync(context: Context) {
            val workRequest: WorkRequest = OneTimeWorkRequestBuilder<SyncDataWorker>()
                .build()
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}
