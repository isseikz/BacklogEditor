package com.isseikz.backlogeditor.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.isseikz.backlogeditor.source.BacklogRepository
import com.isseikz.backlogeditor.store.WidgetProjectRepository
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AddItemDialogActivity : AppCompatActivity() {
    @Inject
    lateinit var backlogRepository: BacklogRepository

    @Inject
    lateinit var widgetProjectRepository: WidgetProjectRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val projectId = intent.getIntExtra(
            android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID,
            android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
        ).also { Timber.d("onCreate with widgetId $it") }
            .takeIf { it != android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID }
            ?.let {
                widgetProjectRepository.widgetProjectMapFlow.value[it] ?: ""
            }

        val fragment = projectId?.let {
            Bundle().apply {
                putString(FragmentProjectPagesScreen.BUNDLE_KEY_PROJECT_ID, projectId)
            }
        }?.let {
            FragmentProjectPagesScreen().apply {
                arguments = it
            }
        } ?: FragmentProjectPagesScreen().also {
            Timber.w("widgetId is invalid")
        }

        // Add FragmentProjectPagesScreen to the activity first
        supportFragmentManager.beginTransaction()
            .add(android.R.id.content, fragment)
            .commit()

        // Show the dialog to input a text and add it to the database on the FragmentProjectPagesScreen
        AddItemDialogFragment.newInstance().apply {
            arguments = Bundle().apply {
                putString(
                    AddItemDialogFragment.BUNDLE_KEY_PROJECT_ID,
                    projectId
                )
            }
        }
            .show(supportFragmentManager, "AddItemDialogFragment")
    }
}
