package com.isseikz.backlogeditor.data

data class ProjectInfo(
    val projectId: String,
    val projectName: String,
    val items: List<BacklogItem>
) {
    companion object {
        const val PROJECT_ID_UNKNOWN = "unknown"
    }
}
