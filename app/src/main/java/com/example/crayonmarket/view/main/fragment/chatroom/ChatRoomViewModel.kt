package com.example.crayonmarket.view.main.fragment.chatroom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.example.crayonmarket.firebase.AuthRepository
import com.example.crayonmarket.firebase.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatRoomViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        ChatRoomUiState(
            currentUserUuid = requireNotNull(
                AuthRepository.currentUserUuid
            )
        )
    )
    val uiState = _uiState.asStateFlow()

    fun bind(
        query: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val pagingFlow = ChatRepository.getMyChatRoom()
            pagingFlow.cachedIn(viewModelScope).collect { pagingData ->
                _uiState.update { uiState ->
                    if (query.isNullOrBlank()) uiState.copy(chatRooms = pagingData.map { it.toUiState() })
                    else uiState.copy(chatRooms = pagingData.map { it.toUiState() }
                        .filter { it.conversationAppliedUserName == query })
                }
            }
        }
    }

    fun errorMessageShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

}