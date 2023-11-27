package com.example.crayonmarket.view.main.fragment.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.crayonmarket.firebase.AuthRepository
import com.example.crayonmarket.firebase.SaleRepository
import com.example.crayonmarket.firebase.UserRepository
import com.example.crayonmarket.view.main.fragment.sale.toUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    fun bind() {
        viewModelScope.launch {
            val result1 =
                UserRepository.getUserDetail(requireNotNull(AuthRepository.currentUserUuid))
            if (result1.isSuccess) {
                _uiState.update { it.copy(userDetail = result1.getOrNull()!!, isLoading = false) }
            }

            val pagingFlow = SaleRepository.getMyFeeds()
            pagingFlow.cachedIn(viewModelScope).collect { pagingData ->
                _uiState.update { uiState ->
                    uiState.copy(salePosts = pagingData.map { it.toUiState() })
                }
            }
        }
    }

    fun errorMessageShown() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }
}