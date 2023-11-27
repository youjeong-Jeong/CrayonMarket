package com.example.crayonmarket.view.main.fragment.chatroom

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.example.crayonmarket.databinding.ItemChattingRoomBinding

class ChatRoomAdapter(
    private val onClickChatItem: (ChatRoomItemUiState) -> Unit
) : PagingDataAdapter<ChatRoomItemUiState, ChatRoomViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemChattingRoomBinding.inflate(layoutInflater, parent, false)
        return ChatRoomViewHolder(
            binding = binding, onClickChatItem = onClickChatItem
        )
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<ChatRoomItemUiState>() {
            override fun areItemsTheSame(
                oldItem: ChatRoomItemUiState, newItem: ChatRoomItemUiState
            ): Boolean {
                return oldItem.uuid == newItem.uuid
            }

            override fun areContentsTheSame(
                oldItem: ChatRoomItemUiState, newItem: ChatRoomItemUiState
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}