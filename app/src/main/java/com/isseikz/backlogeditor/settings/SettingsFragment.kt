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
import com.isseikz.backlogeditor.ui.CreateProjectDialogFragment
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
        preferenceManager.preferenceDataStore = secureTokenStorage


        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val keyGitHubPersonalAccessToken = getString(R.string.preference_key_github_pat)
        val prefToken =
            findPreference<EditTextPreference>(keyGitHubPersonalAccessToken)
        val perfUsername = findPreference<EditTextPreference>(
            getString(R.string.preference_key_github_username)
        )
        val prefProject =
            findPreference<ListPreference>(getString(R.string.preference_key_github_project))

        if (prefToken == null || perfUsername == null || prefProject == null) {
            Timber.w("prefToken or perfUsername or prefProject is null")
            return
        }

        prefToken.setOnPreferenceChangeListener { _, _ ->
                scope.launch {
                    backlogRepository.syncBacklogItems()
                }
                true
            }

        prefProject.apply {
            // update projects when the preference is clicked
            setOnPreferenceClickListener {
                val projectList = backlogRepository.listProjects()
                entries = projectList.map { it.projectName }.toTypedArray() + "Add Project"
                entryValues =
                    projectList.map { it.projectId }.toTypedArray() + ProjectEntry_addProject
                true
            }

            if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val username = perfUsername.text ?: ""

                // set the default value to the preference when the preference is clicked
                setDefaultValue(selectedProjectName)
                setOnPreferenceChangeListener { _, newValue ->
                    val projectId = newValue as String
                    if (projectId == ProjectEntry_addProject) {
                        // open CreateProjectDialogFragment
                        CreateProjectDialogFragment().apply {
                            arguments = Bundle().apply {
                                putString(
                                    CreateProjectDialogFragment.BUNDLE_KEY_USER_ID, username
                                )
                                putString(
                                    CreateProjectDialogFragment.BUNDLE_KEY_SOURCE_NAME,
                                    "github" //TODO: support other sources
                                )
                            }
                        }.show(parentFragmentManager, "CreateProjectDialogFragment")


                        return@setOnPreferenceChangeListener false
                    }
                    widgetProjectRepository.create(widgetId, projectId)
                    // register app widget into home screen
                    registerAppWidget()
                    true
                }
            }
        }

        perfUsername.apply {
            setOnPreferenceChangeListener { preference, newValue ->
                Timber.d("setOnPreferenceChangeListener ${preference.key} $newValue")
                secureTokenStorage.putString(keyGitHubPersonalAccessToken, "")
                prefToken.text = null
                prefProject.entries = emptyArray()
                prefProject.entryValues = emptyArray()
                true
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
                    prefProject?.setDefaultValue(defaultValue)
                    prefProject?.isSelectable = available
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

    companion object {
        private const val ProjectEntry_addProject = "PROJECT_ENTRY_ADD_PROJECT"
    }
}
