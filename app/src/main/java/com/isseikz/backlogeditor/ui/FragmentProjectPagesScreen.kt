package com.isseikz.backlogeditor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.isseikz.backlogeditor.R
import com.isseikz.backlogeditor.source.BacklogRepository
import com.isseikz.backlogeditor.store.WidgetProjectRepository
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

    @Inject
    lateinit var widgetProjectRepository: WidgetProjectRepository

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
        projectPagesAdapter = ProjectPagesAdapter(this)
        viewPager = view.findViewById(R.id.project_items_pager)
        viewPager.adapter = projectPagesAdapter

        val projectId = arguments?.getString(BUNDLE_KEY_PROJECT_ID, "")
        val page = projectId?.let {
            backlogRepository.listProjects().indexOfFirst { project ->
                project.projectId == it
            }
        }?.takeIf { it >= 0 } ?: 0
        Timber.d("page: $page projectId: $projectId")
        viewPager.currentItem = page

        view.findViewById<FloatingActionButton>(R.id.frag_project_page_btn_open_add_item).apply {
            setOnClickListener {
                val currentPageProject =
                    backlogRepository.listProjects()[viewPager.currentItem].projectId
                AddItemDialogFragment.newInstance().apply {
                    arguments = Bundle().apply {
                        putString(
                            AddItemDialogFragment.BUNDLE_KEY_PROJECT_ID,
                            currentPageProject
                        )
                    }
                }
                    .show(requireActivity().supportFragmentManager, "AddItemDialogFragment")
            }
        }

        scope.launch {
            backlogRepository.projectsFlow.collect {
                Timber.d("New projects: $it")
                withContext(Dispatchers.Main) {
                    projectPagesAdapter.notifyDataSetChanged() // TODO
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

    inner class ProjectPagesAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int {
            return backlogRepository.listProjects().size
        }

        override fun createFragment(position: Int): Fragment {
            return FragmentProjectPage(backlogRepository.listProjects()[position].projectId)
        }
    }

    companion object {
        const val BUNDLE_KEY_PROJECT_ID = "projectId"
    }
}
