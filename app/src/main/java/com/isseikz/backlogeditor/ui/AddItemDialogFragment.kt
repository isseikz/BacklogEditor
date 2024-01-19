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
import com.isseikz.backlogeditor.source.BacklogRepository
import com.isseikz.backlogeditor.widget.BacklogAppWidget
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AddItemDialogFragment : DialogFragment() {
    @Inject
    lateinit var backlogRepository: BacklogRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val projectId = arguments?.getString(BUNDLE_KEY_PROJECT_ID) ?: run {
            Timber.w("projectId is null")
            if (BuildConfig.showDebugToast) {
                Toast.makeText(
                    requireContext(),
                    "projectId is null @ AddItemDialogFragment",
                    Toast.LENGTH_SHORT
                ).show()
            }
            throw IllegalArgumentException("projectId is null")
        }
        return inflater.inflate(R.layout.dialog_add_item, container, false).apply {
            val editText = findViewById<EditText>(R.id.editTextNewItem)

            findViewById<ImageButton>(R.id.buttonSubmit).apply {
                setOnClickListener {
                    // disable the button because item is uploading
                    isEnabled = false

                    CoroutineScope(Dispatchers.IO).launch {
                        if (backlogRepository.addBacklogItem(projectId, editText.text.toString())) {
                            BacklogAppWidget.requestUpdate(requireContext())
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    requireContext(),
                                    "New item added",
                                    Toast.LENGTH_SHORT
                                ).show()
                                editText.setText("")
                                isEnabled = true
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    requireContext(),
                                    "Failed to add new item",
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCancelable(true)
        dialog?.setOnCancelListener {
            dismiss()
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

    override fun onDestroyView() {
        dialog?.setOnCancelListener(null)
        super.onDestroyView()
    }

    companion object {
        fun newInstance(): AddItemDialogFragment {
            return AddItemDialogFragment()
        }

        const val BUNDLE_KEY_PROJECT_ID = "project_id"
    }
}
