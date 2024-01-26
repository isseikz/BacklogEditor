package com.isseikz.backlogeditor

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.isseikz.backlogeditor.data.PreferenceKey.Companion.PreferenceName
import com.isseikz.backlogeditor.source.BacklogRepository
import com.isseikz.backlogeditor.source.GitHubBacklogDataSource
import com.isseikz.backlogeditor.source.ProjectRepository
import com.isseikz.backlogeditor.store.SecureTokenStorage
import com.isseikz.backlogeditor.store.WidgetProjectPreferenceWidgetProjectDataStore
import com.isseikz.backlogeditor.store.WidgetProjectRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Provider_BacklogRepository {
    @Provides
    fun provideSecureTokenStorage(@ApplicationContext context: Context): SecureTokenStorage {
        return SecureTokenStorage(context)
    }

    @Provides
    fun provideGitHubBacklogDataSource(secureTokenStorage: SecureTokenStorage): GitHubBacklogDataSource {
        return GitHubBacklogDataSource(secureTokenStorage)
    }

    @Singleton
    @Provides
    fun provideBacklogRepository(gitHubBacklogDataSource: GitHubBacklogDataSource): BacklogRepository {
        return BacklogRepository(listOf(gitHubBacklogDataSource))
    }

    @Singleton
    @Provides
    fun provideWidgetProjectRepository(@ApplicationContext context: Context): WidgetProjectRepository {
        val dataSource = PreferenceDataStoreFactory.create {
            return@create File(context.dataDir, "$PreferenceName.preferences_pb")
        }
        val widgetProjectPreferenceDataStore = WidgetProjectPreferenceWidgetProjectDataStore(dataSource)
        return WidgetProjectRepository(widgetProjectPreferenceDataStore)
    }

    @Singleton
    @Provides
    fun provideProjectRepository(gitHubBacklogDataSource: GitHubBacklogDataSource): ProjectRepository {
        return ProjectRepository(listOf(gitHubBacklogDataSource))
    }
}
