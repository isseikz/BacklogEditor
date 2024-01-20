package com.isseikz.backlogeditor.source

import com.isseikz.backlogeditor.data.BacklogItem
import com.isseikz.backlogeditor.data.BacklogStatus
import com.isseikz.backlogeditor.data.ProjectInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BacklogRepository(
    private val gitHubBacklogDataSource: BacklogDataSource,
) {
    val lastUpdated: Long
        get() = _lastUpdated
    private var _lastUpdated = 0L

    val projectsFlow: StateFlow<Map<String, ProjectInfo>>
        get() = _projectsFlow.asStateFlow()
    private val _projectsFlow: MutableStateFlow<Map<String, ProjectInfo>> = MutableStateFlow(mapOf())
    private val _projects = mutableMapOf<String, ProjectInfo>()

    val projectsAvailabilityFlow: StateFlow<Boolean>
        get() = _projectsAvailabilityFlow.asStateFlow()
    private val _projectsAvailabilityFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    fun getProjectInfo(projectId: String): ProjectInfo? {
        return _projects[projectId]
    }

    fun listProjects(): List<ProjectInfo> {
        return _projects.values.toList()
    }

    suspend fun syncBacklogItems() {
        _projectsAvailabilityFlow.value = false
        val result = gitHubBacklogDataSource.fetchBacklogItems()
        if (result.isSuccess) {
            _projects.clear()
            result.getOrNull()?.forEach { projectInfo ->
                _projects[projectInfo.projectId] = projectInfo
                _projectsFlow.value = _projects.toMap()
            }?.let {
                _projectsAvailabilityFlow.value = true
                _lastUpdated = System.currentTimeMillis()
            }
        }
    }

    suspend fun addBacklogItem(projectId: String, title: String): Boolean {
        if (gitHubBacklogDataSource.addBacklogItem(projectId, title).isSuccess) {
            _projects[projectId]?.items?.let { old ->
                old.toMutableList().plus(BacklogItem(projectId, title, BacklogStatus.TODO, old.size + 1))
            }?.toList()?.let { new ->
                _projects[projectId]?.copy(items = new)
            }?.let { newProject ->
                _projects[projectId] = newProject
                _projectsFlow.value = _projects.toMap()
            }
            return true
        }
        return false
    }
}
