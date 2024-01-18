package com.isseikz.backlogeditor.source

import com.isseikz.backlogeditor.data.BacklogItem
import com.isseikz.backlogeditor.data.ProjectInfo

interface BacklogDataSource {
    suspend fun fetchBacklogItems(): Result<List<ProjectInfo>>
    suspend fun addBacklogItem(projectId: String, title: String): Result<Unit>
}
