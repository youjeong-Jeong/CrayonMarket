package com.example.crayonmarket.view.main.fragment.sale.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crayonmarket.firebase.AuthRepository
import com.example.crayonmarket.firebase.ChatRepository
import com.example.crayonmarket.firebase.SaleRepository
import com.example.crayonmarket.view.main.fragment.sale.toUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SaleDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        SaleDetailUiState(currentUserUuid = requireNotNull(AuthRepository.currentUserUuid))
    )
    val uiState = _uiState.asStateFlow()

    fun bind(
        postUuId: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            check(true)
            val result = SaleRepository.getSaleDetail(postUuId)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        selectedSaleItem = result.getOrNull()!!.toUiState(),
                        currentSelectedSaleItemPossible = result.getOrNull()!!
                            .toUiState().possibleSale
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        errorMessage = result.exceptionOrNull()!!.localizedMessage,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun editOnlySalePossible(uuid: String, currentSelectedSaleItemPossible: Boolean) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result: Result<Unit> =
                SaleRepository.editSaleOnlyPossible(uuid, !currentSelectedSaleItemPossible)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentSelectedSaleItemPossible = !currentSelectedSaleItemPossible
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        errorMessage = "실패!", isLoading = false
                    )
                }
            }
        }
    }

    fun deleteSelectedPost(uiState: SaleDetailUiState) {
        viewModelScope.launch {
            check(true)
            val result = SaleRepository.deleteSale(uiState.selectedSaleItem!!.uuid)
            _uiState.update {
                it.copy(
                    errorMessage = if (result.isSuccess) {
                        "게시물이 삭제되었습니다."
                    } else {
                        "실패!"
                    }, isDeleteSuccess = true
                )
            }
        }
    }

    fun createChattingRoomAndGoCurrentChattingRoom() {
        val postUuid = uiState.value.selectedSaleItem!!.uuid
        val postWriterUuid = uiState.value.selectedSaleItem!!.writerUuid
        viewModelScope.launch {
            check(true)
            val result = ChatRepository.createChattingRoom(
                postUuid = postUuid, postWriterUuid = postWriterUuid
            )
            _uiState.update {
                it.copy(
                    errorMessage = if (result.isSuccess) {
                        "채팅방이 생성되었습니다."
                    } else {
                        "실패!!"
                    }
                )
            }
        }
    }

    fun errorMessageShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
