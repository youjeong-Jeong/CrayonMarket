package com.example.crayonmarket.view.main.fragment.sale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.crayonmarket.firebase.SaleRepository
import com.example.crayonmarket.model.SortType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SaleViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SaleUiState())
    val uiState = _uiState.asStateFlow()

    fun updateSortType(sortType: String) {
        when (sortType) {
            SortType.LATEST_ORDER.sortType -> _uiState.update { it.copy(sortType = SortType.LATEST_ORDER) }
            SortType.HIGH_PRICE_ORDER.sortType -> _uiState.update { it.copy(sortType = SortType.HIGH_PRICE_ORDER) }
            SortType.LOW_PRICE_ORDER.sortType -> _uiState.update { it.copy(sortType = SortType.LOW_PRICE_ORDER) }
        }
    }

    fun updatePossible(flags: Boolean) {
        _uiState.update { it.copy(flags = flags) }
    }

    fun bind() {
        viewModelScope.launch(Dispatchers.IO) {
            val pagingFlow = SaleRepository.getHomeFeeds(uiState.value.sortType, uiState.value.flags)
            pagingFlow.cachedIn(viewModelScope).collect { pagingData ->
                _uiState.update { uiState ->
                    uiState.copy(salePosts = pagingData.map { it.toUiState() })
                }
            }
        }
    }

    fun errorMessageShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}