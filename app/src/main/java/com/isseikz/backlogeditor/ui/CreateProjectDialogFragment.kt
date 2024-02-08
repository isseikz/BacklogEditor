package com.isseikz.backlogeditor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.isseikz.backlogeditor.BuildConfig
import com.isseikz.backlogeditor.R
import com.isseikz.backlogeditor.data.ProjectInfo
import com.isseikz.backlogeditor.source.BacklogRepository
import com.isseikz.backlogeditor.source.ProjectRepository
import com.isseikz.backlogeditor.widget.BacklogAppWidget
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class CreateProjectDialogFragment : DialogFragment() {
    @Inject
    lateinit var backlogRepository: BacklogRepository

    @Inject
    lateinit var projectRepository: ProjectRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val userId = requireArguments().getString(BUNDLE_KEY_USER_ID, "")
            ?.takeIf { it.isNotEmpty() }
            ?: run {
                Timber.w("userId is null")
                if (BuildConfig.showDebugToast) {
                    Toast.makeText(
                        requireContext(),
                        "userId is null @ CreateProjectDialogFragment",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        val sourceName = requireArguments().getString(BUNDLE_KEY_SOURCE_NAME, "")
            ?.takeIf { it.isNotEmpty() }
            ?: run {
                Timber.w("sourceName is null")
                if (BuildConfig.showDebugToast) {
                    Toast.makeText(
                        requireContext(),
                        "sourceName is null @ CreateProjectDialogFragment",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                throw IllegalArgumentException("sourceName is null")
            }
        return inflater.inflate(R.layout.dialog_add_item, container, false).apply {
            val editText = findViewById<EditText>(R.id.editTextNewItem)

            findViewById<ImageButton>(R.id.buttonSubmit).apply {
                setOnClickListener {
                    // disable the button because item is uploading
                    isEnabled = false

                    CoroutineScope(Dispatchers.IO).launch {
                        val projectName = editText.text.toString().trimStart().trimEnd()
                        projectRepository.create(
                            sourceName,
                            ProjectInfo(ProjectInfo.UNDEFINED_PROJECT_ID, projectName, emptyList())
                        ).takeIf { it.isSuccess }?.let {
                            BacklogAppWidget.requestUpdate(requireContext())
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.toast_project_created),
                                    Toast.LENGTH_SHORT
                                ).show()
                                editText.setText("")
                                isEnabled = true
                            }
                        } ?: run {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    requireContext(),
                                    "Failed to create new project",
                                    Toast.LENGTH_SHORT
                                ).show()
                                dismiss()
                            }
                        }

                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawable(null)
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    companion object {
        fun newInstance(): CreateProjectDialogFragment {
            return CreateProjectDialogFragment()
        }

        const val BUNDLE_KEY_USER_ID = "user_id"
        const val BUNDLE_KEY_SOURCE_NAME = "source_name"
    }
}
