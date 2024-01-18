package com.isseikz.backlogeditor.data

data class WidgetProjectPreference(
    val widgetId: Int,
    val projectId: String,
) {
    override fun toString(): String {
        return "WidgetProjectPreference(widgetId=$widgetId, projectId='$projectId')"
    }
}
