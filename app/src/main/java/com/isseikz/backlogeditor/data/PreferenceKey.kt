package com.isseikz.backlogeditor.data

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

class PreferenceKey(private val name: String) {
    fun create(postfix: String): String {
        return "$name/$postfix"
    }

    companion object {
        val PreferenceName = "backlog_editor_preferences"
        private val WidgetProjects = PreferenceKey("widget_project")
        fun WidgetProjectsKey(key: String) = stringPreferencesKey(WidgetProjects.create(key))

        private val GetHubToken = PreferenceKey("github_token")
        fun GetHubTokenKey(key: String = "default") = stringPreferencesKey(GetHubToken.create(key))

        private val WidgetIds = PreferenceKey("widget_ids")
        val WidgetIdsKey = stringSetPreferencesKey(WidgetIds.create("default"))
    }
}
