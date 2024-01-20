package com.isseikz.backlogeditor.source

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.isseikz.backlogeditor.AddDraftMutation
import com.isseikz.backlogeditor.GetProjectsQuery
import com.isseikz.backlogeditor.data.BacklogItem
import com.isseikz.backlogeditor.data.BacklogStatus
import com.isseikz.backlogeditor.data.ProjectInfo
import com.isseikz.backlogeditor.store.SecureTokenStorage

class GitHubBacklogDataSource (
    private val secureTokenStorage: SecureTokenStorage
): BacklogDataSource {
    override suspend fun fetchBacklogItems(): Result<List<ProjectInfo>> {
        val (username, accessToken) = secureTokenStorage.getCredential()?.let { credential ->
            credential.username to credential.token
        } ?: return Result.failure(Exception("Credential not found"))

        val apolloClient = ApolloClient.Builder()
            .serverUrl("https://api.github.com/graphql")
            .addHttpHeader("Authorization", "Bearer $accessToken")
            .build()

        val response: ApolloResponse<GetProjectsQuery.Data> = apolloClient
            .query(GetProjectsQuery(username, 10, 50))
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
}
