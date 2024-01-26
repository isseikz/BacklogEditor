package com.isseikz.backlogeditor.source

import com.isseikz.backlogeditor.data.ProjectInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ProjectRepository(
    private val sources: List<DataSource<ProjectInfo>>,
    scope: CoroutineScope
) {
    val projectsFlow: StateFlow<List<ProjectInfo>>

    init {
        projectsFlow = combine(sources.map { it.dataFlow }) {
            it.toList().flatten()
        }.stateIn(scope, SharingStarted.Lazily, listOf())
    }

    suspend fun create(sourceName: String, projectInfo: ProjectInfo): Result<Unit> {
        val source = sources.firstOrNull { it.name == sourceName }
            ?: return Result.failure(IllegalStateException("Source not found"))
        return source.create(projectInfo)
    }

    companion object {
        fun createWithIODispatchers(
            sources: List<DataSource<ProjectInfo>>
        ): ProjectRepository {
            val scope = CoroutineScope(Dispatchers.IO)
            return ProjectRepository(sources, scope)
        }
    }
}
