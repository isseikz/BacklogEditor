package com.isseikz.backlogeditor.settings

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.isseikz.backlogeditor.R
import com.isseikz.backlogeditor.SyncDataWorker
import com.isseikz.backlogeditor.source.BacklogRepository
import com.isseikz.backlogeditor.store.SecureTokenStorage
import com.isseikz.backlogeditor.store.WidgetProjectRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var backlogRepository: BacklogRepository

    @Inject
    lateinit var secureTokenStorage: SecureTokenStorage

    @Inject
    lateinit var widgetProjectRepository: WidgetProjectRepository

    private val selectedProjectName: String
        get() = widgetProjectRepository.widgetProjectMapFlow.value[widgetId] ?: ""


    private val scope = CoroutineScope(Dispatchers.IO)
    private val widgetId: Int by lazy {
        arguments?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val accessToken = secureTokenStorage.getAccessToken()

        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        if (!accessToken.isNullOrEmpty()) {
            findPreference<EditTextPreference>("personal_access_token")?.apply {
                setDefaultValue(accessToken)

                // save token to secure storage when the preference is changed
                setOnPreferenceChangeListener { _, newValue ->
                    secureTokenStorage.storeAccessToken(newValue as String)
                    scope.launch {
                        backlogRepository.syncBacklogItems()
                    }
                    true
                }
            }
        }


        val projectPreference = findPreference<ListPreference>("project")!!
        projectPreference.apply {

            // update projects when the preference is clicked
            setOnPreferenceClickListener {
                val projects = backlogRepository.listProjects()
                entries = projects.map { it.projectName }.toTypedArray()
                entryValues = projects.map { it.projectId }.toTypedArray()
                true
            }

            if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                // set the default value to the preference when the preference is clicked
                val defaultValue =
                    widgetProjectRepository.widgetProjectMapFlow.value[widgetId] ?: ""
                setDefaultValue(selectedProjectName)
                setOnPreferenceChangeListener { preference, newValue ->
                    val projectId = newValue as String
                    widgetProjectRepository.create(widgetId, projectId)
                    // register app widget into home screen
                    registerAppWidget()
                    true
                }
            }
        }

        String.format(
            getString(R.string.about_how_to_use_app_description),
            getString(R.string.link_to_create_token_title_github),
            getString(R.string.project_title)
        ).also {
            findPreference<Preference>("preference_about_how_to_use")?.apply {
                summary = it
            }
        }

        scope.launch {
            backlogRepository.projectsAvailabilityFlow.collect { available ->
                val defaultValue = when {
                    !available -> getString(R.string.project_default_value_syncing)
                    selectedProjectName.isEmpty() -> getString(R.string.project_default_value_not_selected)
                    else -> selectedProjectName
                }
                withContext(Dispatchers.Main) {
                    projectPreference.setDefaultValue(defaultValue)
                    projectPreference.isSelectable = available
                }
            }
        }
    }

    private fun registerAppWidget() {
        Timber.d("registerAppWidget $widgetId")
        SyncDataWorker.requestOneTimeSync(requireContext())
        val result = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        requireActivity().setResult(Activity.RESULT_OK, result)
        requireActivity().finish()
    }
}