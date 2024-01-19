package com.isseikz.backlogeditor.settings

import android.appwidget.AppWidgetManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.isseikz.backlogeditor.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set title of the activity with string resource
        title = String.format(
            getString(R.string.title_activity_settings),
            getString(R.string.app_name)
        )

        val fragment = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ).takeIf { it != AppWidgetManager.INVALID_APPWIDGET_ID }?.let {
            Bundle().apply {
                putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, it)
            }
        }?.let { bundle ->
            SettingsFragment().apply {
                arguments = bundle
            }
        } ?: SettingsFragment()


        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, fragment)
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}