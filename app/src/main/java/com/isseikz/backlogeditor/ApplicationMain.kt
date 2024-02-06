package com.isseikz.backlogeditor

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.isseikz.backlogeditor.source.BacklogRepository
import com.isseikz.backlogeditor.widget.BacklogAppWidget
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class ApplicationMain: Application(), Configuration.Provider  {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()

    @Inject
    lateinit var backlogRepository: BacklogRepository


    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.d("onCreate")

        CoroutineScope(Dispatchers.IO).launch {
            backlogRepository.projectsAvailabilityFlow.collect {
                withContext(Dispatchers.Main) {
                    BacklogAppWidget.requestUpdate(this@ApplicationMain)
                }
            }
        }
    }
}
