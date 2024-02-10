package com.isseikz.backlogeditor.source

import com.isseikz.backlogeditor.data.ProjectInfo

interface BacklogDataSource {
    suspend fun fetchBacklogItems(): Result<List<ProjectInfo>>
    suspend fun fetchBacklogItems(projectId: String): Result<ProjectInfo>
    suspend fun addBacklogItem(projectId: String, title: String): Result<Unit>
}
