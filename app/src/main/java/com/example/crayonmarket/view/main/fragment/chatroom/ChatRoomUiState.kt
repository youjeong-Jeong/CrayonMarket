package com.example.crayonmarket.view.main.fragment.chatroom

import androidx.paging.PagingData
import com.example.crayonmarket.model.ChatRoom

data class ChatRoomUiState(
    val chatRooms: PagingData<ChatRoomItemUiState> = PagingData.empty(),
    val currentUserUuid: String,
    val errorMessage: String? = null
)

data class ChatRoomItemUiState(
    val uuid: String,
    val conversationAppliedUserName: String,
    val conversationAppliedUserProfileImage: String?
)

fun ChatRoom.toUiState() = ChatRoomItemUiState(
    uuid = uuid,
    conversationAppliedUserName = conversationAppliedUserName,
    conversationAppliedUserProfileImage = conversationAppliedUserProfileImage
)