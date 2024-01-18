package com.isseikz.backlogeditor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.isseikz.backlogeditor.R
import com.isseikz.backlogeditor.data.BacklogItem
import com.isseikz.backlogeditor.data.ProjectInfo
import com.isseikz.backlogeditor.source.BacklogRepository
import com.isseikz.backlogeditor.store.WidgetProjectRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class FragmentProjectPage(private val projectId: String): Fragment() {
    @Inject
    lateinit var backlogRepository: BacklogRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_project_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val project = backlogRepository.getProjectInfo(projectId) ?: run {
            Timber.w("project is null with projectId $projectId")
            return
        }

        view.findViewById<TextView>(R.id.title_project).text = project.projectName

        view.findViewById<RecyclerView>(R.id.items_list).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ProjectItemsAdapter(backlogRepository)
        }
    }

    inner class ProjectItemsAdapter(
        private val backlogRepository: BacklogRepository
    ) : RecyclerView.Adapter<ProjectItemsAdapter.ProjectItemViewHolder>() {
        private val scope = CoroutineScope(Dispatchers.IO)
        val projects: List<BacklogItem>
            get() = backlogRepository.projectsFlow.value[projectId]?.items ?: listOf()

        init {
            scope.launch {
                backlogRepository.projectsFlow.collect {
                    withContext(Dispatchers.Main) {
                        notifyDataSetChanged()
                    }
                }
            }
        }

        inner class ProjectItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            fun bind(title: String) {
                itemView.findViewById<TextView>(R.id.project_list_item_title).text = title
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectItemViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.project_list_item, parent, false)
            return ProjectItemViewHolder(view)
        }

        override fun getItemCount(): Int {
            return projects.size
        }

        override fun onBindViewHolder(holder: ProjectItemViewHolder, position: Int) {
            holder.bind(projects[position].title)
        }
    }
}
