package com.isseikz.backlogeditor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.isseikz.backlogeditor.R
import com.isseikz.backlogeditor.data.BacklogItem
import com.isseikz.backlogeditor.source.BacklogRepository
import dagger.hilt.android.AndroidEntryPoint
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
        val singleProjectAdapter = ProjectItemsAdapter()
        val tvProjectTitle = view.findViewById<TextView>(R.id.title_project)
        view.findViewById<RecyclerView>(R.id.items_list).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = singleProjectAdapter
        }
        viewLifecycleOwner.lifecycleScope.launch {
            backlogRepository.projectsFlow.collect { projects ->
                projects[projectId]
                    ?.let {
                        withContext(Dispatchers.Main) {
                            tvProjectTitle.text = it.projectName
                            singleProjectAdapter.submitList(it.items)
                        }
                    } ?: run {
                    Timber.w("project is null with projectId $projectId")
                }
            }
        }
    }

    class ProjectItemsAdapter :
        ListAdapter<BacklogItem, ProjectItemsAdapter.ProjectItemViewHolder>(BacklogItemDiffCallback()) {
        inner class ProjectItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            fun bind(title: String) {
                itemView.findViewById<TextView>(R.id.project_list_item_title).text = title
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.project_list_item, parent, false)
            return ProjectItemViewHolder(view)
        }

        override fun onBindViewHolder(holder: ProjectItemViewHolder, position: Int) {
            holder.bind(getItem(position).title)
        }
    }

    class BacklogItemDiffCallback : DiffUtil.ItemCallback<BacklogItem>() {
        override fun areItemsTheSame(oldItem: BacklogItem, newItem: BacklogItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BacklogItem, newItem: BacklogItem): Boolean {
            return oldItem == newItem
        }
    }
}
