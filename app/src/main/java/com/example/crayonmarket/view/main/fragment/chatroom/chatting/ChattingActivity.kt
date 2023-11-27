package com.example.crayonmarket.view.main.fragment.chatroom.chatting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.crayonmarket.databinding.ActivityChattingBinding
import kotlinx.coroutines.launch

class ChattingActivity : AppCompatActivity() {

    companion object {
        fun getIntent(
            context: Context, roomUuid: String
        ): Intent {
            return Intent(context, ChattingActivity::class.java).apply {
                putExtra("roomUuid", roomUuid)
            }
        }
    }

    private val viewModel: ChattingViewModel by viewModels()
    private lateinit var binding: ActivityChattingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChattingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val roomUuid = intent.getStringExtra("roomUuid")
        viewModel.bind(roomUuid = requireNotNull(roomUuid))
        initEvent()

        val adapter = ChattingAdapter()
        initRecyclerView(adapter)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    updateUi(it, adapter)
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()

        val roomUuid = intent.getStringExtra("roomUuid")
        viewModel.bind(roomUuid = requireNotNull(roomUuid))
    }


    private fun initEvent() = with(binding) {
        sendButton.setOnClickListener {
            viewModel.sendMessage()
            editChatMessage.setText("")

            recyclerView.layoutManager = LinearLayoutManager(this@ChattingActivity).apply {
                this.stackFromEnd = true    // 가장 최근의 대화를 표시하기 위해 맨 아래로 정렬.
            }
        }

        editChatMessage.addTextChangedListener {
            if (it != null) {
                viewModel.updateMessage(it.toString())
            }
        }


    }

    private fun initRecyclerView(adapter: ChattingAdapter) = with(binding) {
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this@ChattingActivity).apply {
            this.stackFromEnd = true    // 가장 최근의 대화를 표시하기 위해 맨 아래로 정렬.
        }
    }

    private fun updateUi(uiState: ChattingUiState, adapter: ChattingAdapter) = with(binding) {
        adapter.submitList(uiState.chats)
        if (uiState.chats != null) {
            emptyText.isVisible = uiState.chats.isEmpty()
        }

        if (uiState.currentChatItemUiState != null) {
            otherUserName.text = uiState.currentChatItemUiState.conversationAppliedUserName
        }

        progressBar.isVisible = uiState.isLoading
    }


}