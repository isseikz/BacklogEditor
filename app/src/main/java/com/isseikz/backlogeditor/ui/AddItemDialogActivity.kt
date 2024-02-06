package com.isseikz.backlogeditor.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AddItemDialogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.getStringExtra(EXTRA_PROJECT_ID)
            .also { Timber.d("onCreate with projectId $it")}
            ?.let {
                supportFragmentManager.beginTransaction()
                    .add(android.R.id.content, FragmentProjectPagesScreen.newInstance(it))
                    .commit()
                AddItemDialogFragment.newInstance(it)
                    .show(supportFragmentManager, "AddItemDialogFragment")
            } ?: run {
                Timber.w("projectId is null")
                throw IllegalArgumentException("projectId is null")
            }
    }

    companion object {
        const val EXTRA_PROJECT_ID = "com.isseikz.backlogeditor.ui.AddItemDialogActivity.EXTRA_PROJECT_ID"
        fun createIntent(context: Context, projectId: String): Intent {
            Timber.d("createIntent with projectId $projectId")
            return Intent(context, AddItemDialogActivity::class.java).apply {
                putExtra(EXTRA_PROJECT_ID, projectId)
            }
        }
    }
}
