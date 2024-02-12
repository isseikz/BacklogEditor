package com.isseikz.backlogeditor.data

import kotlinx.serialization.Serializable

@Serializable
data class WidgetProjectPreference(
    val widgetId: Int,
    val projectId: String,
    val statusFilter: Set<BacklogStatus>
) {
    override fun toString(): String {
        return "WidgetProjectPreference(widgetId=$widgetId, projectId='$projectId')"
    }

    class Builder {
        private var widgetId: Int = 0
        private var projectId: String = ""
        private var statusFilter: Set<BacklogStatus> = emptySet()

        fun widgetId(widgetId: Int) = apply { this.widgetId = widgetId }
        fun projectId(projectId: String) = apply { this.projectId = projectId }
        fun statusFilter(statusFilter: Set<BacklogStatus>) =
            apply { this.statusFilter = statusFilter }

        fun build() = WidgetProjectPreference(widgetId, projectId, statusFilter)
    }
}
