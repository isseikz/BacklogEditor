package com.isseikz.backlogeditor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.isseikz.backlogeditor.R
import com.isseikz.backlogeditor.data.ProjectInfo
import com.isseikz.backlogeditor.source.BacklogRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

// // Create a new fragment to show the list of items of each project with ViewPager2
@AndroidEntryPoint
class FragmentProjectPagesScreen : Fragment() {
    @Inject
    lateinit var backlogRepository: BacklogRepository

    private lateinit var projectPagesAdapter: ProjectPagesAdapter
    private lateinit var viewPager: androidx.viewpager2.widget.ViewPager2
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_project_pages_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var projectId = arguments?.getString(BUNDLE_KEY_PROJECT_ID)

        projectPagesAdapter = ProjectPagesAdapter(this, ProjectItemDiffCallback())
        viewPager = view.findViewById(R.id.project_items_pager)
        viewPager.adapter = projectPagesAdapter
        viewPager.registerOnPageChangeCallback(object :
            androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Timber.d("onPageSelected: $position")
                projectId = backlogRepository.listProjects()[position].projectId
            }
        })


        view.findViewById<FloatingActionButton>(R.id.frag_project_page_btn_open_add_item).apply {
            setOnClickListener {
                val currentPageProject =
                    backlogRepository.listProjects()[viewPager.currentItem].projectId
                AddItemDialogFragment.newInstance(currentPageProject)
                    .show(requireActivity().supportFragmentManager, "AddItemDialogFragment")
            }
        }

        lifecycleScope.launch {
            backlogRepository.projectsFlow.collect { projects ->
                Timber.d("New projects: $projects")

                projectId?.let { id ->
                    projects.values.indexOfFirst { it.projectId == id }
                }?.takeIf { it >= 0 }?.let {
                    Timber.d("page: $it projectId: $projectId")
                    viewPager.currentItem = it
                }

                withContext(Dispatchers.Main) {
                    projectPagesAdapter.updateItems(projects.values.toList())
                }
            }
        }
    }

    // sync the data with the database when the fragment is resumed
    override fun onResume() {
        super.onResume()
        Timber.d("onResume")
        scope.launch {
            backlogRepository.syncBacklogItems()
        }
    }

    inner class ProjectPagesAdapter(
        fragment: Fragment,
        private val diffCallback: DiffUtil.ItemCallback<ProjectInfo>
    ) : FragmentStateAdapter(fragment) {
        private var items = listOf<ProjectInfo>()

        fun updateItems(newItems: List<ProjectInfo>) {
            val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int = items.size

                override fun getNewListSize(): Int = newItems.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return diffCallback.areItemsTheSame(
                        items[oldItemPosition],
                        newItems[newItemPosition]
                    )
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    return diffCallback.areContentsTheSame(
                        items[oldItemPosition],
                        newItems[newItemPosition]
                    )
                }
            })

            items = newItems
            diffResult.dispatchUpdatesTo(this)
        }

        override fun getItemCount(): Int {
            Timber.d("getItemCount: ${backlogRepository.listProjects().size}")
            return items.size
        }

        override fun createFragment(position: Int): Fragment {
            Timber.d("createFragment: $position")
            return FragmentProjectPage(items[position].projectId)
        }
    }

    class ProjectItemDiffCallback : DiffUtil.ItemCallback<ProjectInfo>() {
        override fun areItemsTheSame(oldItem: ProjectInfo, newItem: ProjectInfo): Boolean {
            return oldItem.projectId == newItem.projectId
        }

        override fun areContentsTheSame(oldItem: ProjectInfo, newItem: ProjectInfo): Boolean {
            return oldItem == newItem
        }
    }

    companion object {
        const val BUNDLE_KEY_PROJECT_ID = "projectId"
        fun newInstance(projectId: String): FragmentProjectPagesScreen {
            return FragmentProjectPagesScreen().apply {
                arguments = Bundle().apply {
                    putString(BUNDLE_KEY_PROJECT_ID, projectId)
                }
            }
        }
    }
}
