package com.isseikz.backlogeditor.data

data class ProjectInfo(
    val projectId: String,
    val projectName: String,
    val items: List<BacklogItem>
) {
    companion object {
        const val UNDEFINED_PROJECT_ID = "undefined_project_id"
    }
}
