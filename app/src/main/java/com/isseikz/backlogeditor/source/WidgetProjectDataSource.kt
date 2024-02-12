package com.isseikz.backlogeditor.source

import com.isseikz.backlogeditor.data.WidgetProjectPreference
import kotlinx.coroutines.flow.Flow

interface WidgetProjectDataSource {
    val preferenceFlow: Flow<List<WidgetProjectPreference>>
    suspend fun create(
        widgetId: Int,
        preference: WidgetProjectPreference
    )

    suspend fun delete(widgetId: Int)
}
