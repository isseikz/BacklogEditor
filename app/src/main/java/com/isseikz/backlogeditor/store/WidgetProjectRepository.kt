package com.isseikz.backlogeditor.store

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
    val widgetProjectMapFlow: StateFlow<Map<Int, String>>
    private val _widgetProjectMap: MutableStateFlow<Map<Int, String>> = MutableStateFlow(mapOf())
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        widgetProjectMapFlow = _widgetProjectMap.asStateFlow()

        scope.launch {
            widgetProjectPreferenceDataStore.preferenceFlow.collect { widgetProjectPreference ->
                logger.d("widgetProjectPreference: $widgetProjectPreference")
                _widgetProjectMap.value =
                    widgetProjectPreference.associate { it.widgetId to it.projectId }
            }
        }
    }

    fun create(widgetId: Int, projectId: String) {
        logger.d("create widgetId: $widgetId, projectId: $projectId")
        scope.launch {
            widgetProjectPreferenceDataStore.create(widgetId, projectId)
        }
    }
}
