package com.isseikz.backlogeditor.settings

import android.appwidget.AppWidgetManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.isseikz.backlogeditor.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fragment = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID).takeIf { it != AppWidgetManager.INVALID_APPWIDGET_ID }?.let {
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