package com.isseikz.backlogeditor.source

import com.isseikz.backlogeditor.data.BacklogItem
import com.isseikz.backlogeditor.data.BacklogStatus
import com.isseikz.backlogeditor.data.ProjectInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BacklogRepository(
    private val backlogDataSources: List<BacklogDataSource>,
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

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            syncBacklogItems()
        }
    }

    fun getProjectInfo(projectId: String): ProjectInfo? {
        return _projects[projectId]
    }

    fun listProjects(): List<ProjectInfo> {
        return _projects.values.toList()
    }

    suspend fun syncBacklogItems() {
        _projectsAvailabilityFlow.update { _ -> false }
        backlogDataSources.forEach {
            it.fetchBacklogItems().getOrNull()?.forEach { newInfo ->
                updateProjectInfo(newInfo)
            } ?: run {
                _projectsAvailabilityFlow.update { _ -> false }
            }
        }
    }

    suspend fun syncBacklogItems(projectId: String) {
        backlogDataSources.forEach {
            it.fetchBacklogItems(projectId).getOrNull()?.let { newInfo ->
                updateProjectInfo(newInfo)
            } ?: run {
                _projectsAvailabilityFlow.update { _ -> false }
            }
        }
    }

    suspend fun addBacklogItem(projectId: String, title: String): Boolean {
        val result = backlogDataSources.firstOrNull()
            ?.addBacklogItem(projectId, title)
            ?: return false

        if (result.isSuccess) {
            _projects[projectId]?.items?.let { old ->
                old.toMutableList()
                    .plus(BacklogItem(projectId, title, BacklogStatus.TODO, old.size + 1))
            }?.toList()?.let { new ->
                _projects[projectId]?.copy(items = new)
            }?.let { newProject ->
                _projects[projectId] = newProject
                _projectsFlow.value = _projects.toMap()
            }
        } else {
            result.exceptionOrNull()?.printStackTrace()
        }

        return result.isSuccess
    }

    private fun updateProjectInfo(newInfo: ProjectInfo) {
        _projects[newInfo.projectId] = newInfo
        _projectsFlow.update { oldProjects ->
            oldProjects.toMutableMap().apply {
                this[newInfo.projectId] = newInfo
            }.toMap()
        }
        _lastUpdated = System.currentTimeMillis()
        _projectsAvailabilityFlow.update { _ -> true }
    }
}
