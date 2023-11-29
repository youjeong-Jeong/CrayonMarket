package com.example.crayonmarket.view.main.fragment.chatroom

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.crayonmarket.R
import com.example.crayonmarket.databinding.FragmentChatRoomBinding
import com.example.crayonmarket.view.common.PagingLoadStateAdapter
import com.example.crayonmarket.view.common.ViewBindingFragment
import com.example.crayonmarket.view.common.addDividerDecoration
import com.example.crayonmarket.view.common.hideKeyboard
import com.example.crayonmarket.view.common.registerObserverForScrollToTop
import com.example.crayonmarket.view.common.setListeners
import com.example.crayonmarket.view.main.fragment.chatroom.chatting.ChattingActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ChatRoomFragment : ViewBindingFragment<FragmentChatRoomBinding>() {

    private val viewModel: ChatRoomViewModel by activityViewModels()
    private lateinit var launcher: ActivityResultLauncher<Intent>

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentChatRoomBinding
        get() = FragmentChatRoomBinding::inflate


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bind(null)
        val adapter = ChatRoomAdapter(::onClickChatItem)
        initRecyclerView(adapter)
        initEvent()

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                adapter.refresh()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    updateUi(it, adapter)
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.bind(null)
    }

    private fun initEvent() = with(binding) {
        searchBar.isSubmitButtonEnabled = true
        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    viewModel.bind(query)
                    hideKeyboard()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun initRecyclerView(adapter: ChatRoomAdapter) = with(binding) {
        recyclerView.adapter =
            adapter.withLoadStateFooter(PagingLoadStateAdapter { adapter.retry() })
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.addDividerDecoration()
        loadState.emptyText.text = getString(R.string.is_not_chatting_room)
        loadState.emptyText.textSize = 16.0F
        loadState.setListeners(adapter, swipeRefreshLayout)
        adapter.registerObserverForScrollToTop(recyclerView)
    }

    private fun updateUi(uiState: ChatRoomUiState, adapter: ChatRoomAdapter) {
        adapter.submitData(viewLifecycleOwner.lifecycle, uiState.chatRooms)

        if (uiState.errorMessage != null) {
            viewModel.errorMessageShown()
            showSnackBar(uiState.errorMessage)
        }

    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun onClickChatItem(chatRoomItemUiState: ChatRoomItemUiState) {
        val intent = ChattingActivity.getIntent(
            requireContext(), chatRoomItemUiState.uuid
        )
        launcher.launch(intent)
    }
}