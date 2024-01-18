package com.isseikz.backlogeditor.data

data class ProjectInfo(
    val projectId: String,
    val projectName: String,
    val items: List<BacklogItem>
)
