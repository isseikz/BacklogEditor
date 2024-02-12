package com.isseikz.backlogeditor.store

import com.isseikz.backlogeditor.data.WidgetProjectPreference
import com.isseikz.backlogeditor.multiplatformlogger.Logger
import com.isseikz.backlogeditor.source.WidgetProjectDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WidgetProjectRepository(
    private val widgetProjectPreferenceDataStore: WidgetProjectDataSource,
    private val logger: Logger
) {
    val widgetProjectMapFlow: StateFlow<Map<Int, WidgetProjectPreference>>
    private val _widgetProjectMap: MutableStateFlow<Map<Int, WidgetProjectPreference>> =
        MutableStateFlow(mapOf())
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        widgetProjectMapFlow = _widgetProjectMap.asStateFlow()

        scope.launch {
            widgetProjectPreferenceDataStore.preferenceFlow.collect { widgetProjectPreference ->
                logger.d("widgetProjectPreference: $widgetProjectPreference")
                _widgetProjectMap.value =
                    widgetProjectPreference.associateBy { it.widgetId }
            }
        }
    }

    fun create(widgetId: Int, preference: WidgetProjectPreference) {
        logger.d("create widgetId: $widgetId, projectId: ${preference.projectId}")
        scope.launch {
            widgetProjectPreferenceDataStore.create(widgetId, preference)
        }
    }
}
