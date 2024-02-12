package com.isseikz.backlogeditor.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.isseikz.backlogeditor.data.BacklogStatus
import com.isseikz.backlogeditor.data.PreferenceKey
import com.isseikz.backlogeditor.data.WidgetProjectPreference
import com.isseikz.backlogeditor.source.WidgetProjectDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber

class WidgetProjectPreferenceWidgetProjectDataStore(
    private val dataStore: DataStore<Preferences>,
) : WidgetProjectDataSource {
    val widgetIdsFlow: Flow<Set<Int>> = dataStore.data.map { preference ->
        preference[PreferenceKey.WidgetIdsKey]?.map { it.toInt() }?.toSet() ?: setOf()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val preferenceFlow: Flow<List<WidgetProjectPreference>> = widgetIdsFlow.flatMapConcat { widgetIds ->
        flowOf(
            widgetIds.associateWith {widgetId ->
                dataStore.data.map { preference ->
                    preference[PreferenceKey.WidgetProjectsKey("$widgetId")]
                }.firstOrNull() ?: ""
            }.map {
                Timber.d("widgetProjectPreference: $it")
                if (it.value.startsWith("{") && it.value.endsWith("}")) {
                    Json.decodeFromString<WidgetProjectPreference>(it.value)
                } else {
                    WidgetProjectPreference.Builder()
                        .widgetId(it.key)
                        .projectId(it.value)
                        .statusFilter(
                            setOf(
                                BacklogStatus.TODO,
                                BacklogStatus.IN_PROGRESS,
                                BacklogStatus.DONE
                            )
                        )
                        .build()
                }
            }
        )
    }

    override suspend fun create(widgetId: Int, preference: WidgetProjectPreference) {
        val newItem = Json.encodeToString(preference)
        dataStore.edit { store ->
            store[PreferenceKey.WidgetIdsKey] =
                store[PreferenceKey.WidgetIdsKey]?.toMutableSet()?.apply {
                    add(widgetId.toString())
                } ?: setOf(widgetId.toString())
            store[PreferenceKey.WidgetProjectsKey("$widgetId")] = newItem
        }
    }

    override suspend fun delete(widgetId: Int) {
        dataStore.edit { preference ->
            preference[PreferenceKey.WidgetIdsKey] =
                preference[PreferenceKey.WidgetIdsKey]?.toMutableSet()?.apply {
                    remove(widgetId.toString())
                } ?: setOf(widgetId.toString())
            preference.remove(PreferenceKey.WidgetProjectsKey("$widgetId"))
        }
    }
}
