package com.isseikz.backlogeditor.source

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.isseikz.backlogeditor.AddDraftMutation
import com.isseikz.backlogeditor.AddUserProjectMutation
import com.isseikz.backlogeditor.GetProjectsQuery
import com.isseikz.backlogeditor.GetUserGlobalIdQuery
import com.isseikz.backlogeditor.data.BacklogItem
import com.isseikz.backlogeditor.data.BacklogStatus
import com.isseikz.backlogeditor.data.ProjectInfo
import com.isseikz.backlogeditor.multiplatformlogger.Logger
import com.isseikz.backlogeditor.store.SecureTokenStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GitHubBacklogDataSource(
    private val secureTokenStorage: SecureTokenStorage,
    private val logger: Logger
) : BacklogDataSource, DataSource<ProjectInfo> {
    override suspend fun fetchBacklogItems(): Result<List<ProjectInfo>> {
        val (username, accessToken) = credential
            ?: return Result.failure(Exception("Credential not found"))

        val response: ApolloResponse<GetProjectsQuery.Data> = client(accessToken)
            .query(GetProjectsQuery(username, NUM_OF_PROJECTS, NUM_OF_ITEMS_PER_PROJECT))
            .execute()

        // Output response details to logcat with Timber

        return if (response.hasErrors()) {
            Result.failure(Exception(response.errors?.firstOrNull()?.message))
        } else {
            val backlogItems = response.data?.user?.recentProjects?.nodes?.mapNotNull { project ->
                val projectItems = project?.onProjectV2?.items?.nodes
                val items = projectItems?.mapIndexedNotNull { index, item ->
                    val projectItem = item?.onProjectV2Item
                    BacklogItem(
                        id = projectItem?.id ?: "",
                        title = projectItem?.content?.title ?: "",
                        status = projectItem?.content?.status ?: BacklogStatus.TODO,
                        priority = index
                    )
                }

                val projectInfo = ProjectInfo(
                    projectId = project?.onProjectV2?.id ?: "",
                    projectName = project?.onProjectV2?.title ?: "",
                    items = items ?: emptyList()
                )

                projectInfo
            } ?: emptyList()
            Result.success(backlogItems)
        }
    }

    override suspend fun addBacklogItem(projectId: String, title: String): Result<Unit> {
        val accessToken = secureTokenStorage.getAccessToken()

        val apolloClient = ApolloClient.Builder()
            .serverUrl("https://api.github.com/graphql")
            .addHttpHeader("Authorization", "Bearer $accessToken")
            .build()


        val response: ApolloResponse<AddDraftMutation.Data> = apolloClient
            .mutation(AddDraftMutation(projectId, title))
            .execute()

        return if (response.hasErrors()) {
            Result.failure(Exception(response.errors?.firstOrNull()?.message))
        } else {
            Result.success(Unit)
        }
    }

    val GetProjectsQuery.Content.title: String
        get() = onDraftIssue?.title ?: onIssue?.title ?: onPullRequest?.title ?: ""

    val GetProjectsQuery.Content.closed: Boolean
        get() = onIssue?.closed ?: onPullRequest?.closed ?: false

    val GetProjectsQuery.Content.status: BacklogStatus
        get() = when {
            onIssue?.closed == true || onPullRequest?.closed == true -> BacklogStatus.DONE
            onIssue?.closed == false && onPullRequest?.closed == false -> BacklogStatus.IN_PROGRESS
            else -> BacklogStatus.TODO // onDraftIssue
        }

    override val name: String
        get() = "github"
    override val dataFlow: StateFlow<List<ProjectInfo>>
        get() = _projectsFlow.asStateFlow()
    private val _projectsFlow = MutableStateFlow<List<ProjectInfo>>(listOf())

    override suspend fun create(data: ProjectInfo): Result<Unit> {
        val (username, token) = credential
            ?: return Result.failure(Exception("Credential not found"))

        return client(token).query(GetUserGlobalIdQuery()).execute().let {
            if (it.hasErrors()) {
                logger.d("it.errors?.firstOrNull()?.message = ${it.errors?.firstOrNull()?.message}")
                return Result.failure(Exception(it.errors?.firstOrNull()?.message))
            }
            it.data?.viewer?.id
        }?.let { userId ->
            client(token)
                .mutation(AddUserProjectMutation(userId, data.projectName))
        }?.execute()
            ?.let {
                if (it.hasErrors()) {
                    logger.d("it.errors?.firstOrNull()?.message = ${it.errors?.firstOrNull()?.message}")
                    Result.failure(Exception(it.errors?.firstOrNull()?.message))
                } else {
                    it.data?.createProjectV2?.projectV2?.let {newProject ->
                        _projectsFlow.update {oldProjects ->
                            oldProjects + ProjectInfo(
                                projectId = newProject.id,
                                projectName = newProject.title,
                                items = emptyList()
                            )
                        }
                    }
                    Result.success(Unit)
                }
            } ?: Result.failure(Exception("Failed to create project"))
    }

    override suspend fun read(): Result<List<ProjectInfo>> {
        TODO("Not yet implemented")
    }

    override suspend fun update(data: ProjectInfo): Result<ProjectInfo> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(data: ProjectInfo): Result<ProjectInfo> {
        TODO("Not yet implemented")
    }

    private val credential: Pair<String, String>?
        get() = secureTokenStorage.getCredential()?.let { credential ->
            credential.username to credential.token
        }

    companion object {
        private const val NUM_OF_PROJECTS = 50
        private const val NUM_OF_ITEMS_PER_PROJECT = 50
        private fun client(accessToken: String) = ApolloClient.Builder()
            .serverUrl("https://api.github.com/graphql")
            .addHttpHeader("Authorization", "Bearer $accessToken")
            .build()
    }
}
