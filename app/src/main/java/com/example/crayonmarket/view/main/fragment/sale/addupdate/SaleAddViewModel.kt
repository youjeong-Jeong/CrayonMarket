package com.example.crayonmarket.view.main.fragment.sale.addupdate

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crayonmarket.R
import com.example.crayonmarket.firebase.SaleRepository
import com.example.crayonmarket.view.main.fragment.sale.toUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SaleAddViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SaleAddUiState())
    val uiState = _uiState.asStateFlow()

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateContent(content: String) {
        _uiState.update { it.copy(content = content) }
    }

    fun updateCost(cost: String) {
        _uiState.update { it.copy(cost = cost) }
    }

    fun selectImage(selectedImage: Uri) {
        _uiState.update { it.copy(selectedImage = selectedImage) }
    }

    fun changeToEditMode() {
        _uiState.update { it.copy(isCreating = false) }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun uploadSale() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = SaleRepository.uploadSale(
                title = uiState.value.title,
                content = uiState.value.content,
                imageUri = uiState.value.selectedImage!!,
                cost = uiState.value.cost
            )
            if (result.isSuccess) {
                _uiState.update { it.copy(successToUpload = true, isLoading = false) }
            } else {
                _uiState.update {
                    it.copy(
                        errorMessage = R.string.failed, isLoading = false
                    )
                }
            }
        }
    }

    fun editContent(uuid: String) {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val result = SaleRepository.editSale(
                uuid = uuid,
                title = uiState.value.title,
                content = uiState.value.content,
                imageUri = uiState.value.selectedImage!!,
                cost = uiState.value.cost
            )

            if (result.isSuccess) {
                _uiState.update { it.copy(successToUpload = true, isLoading = false) }
            } else {
                _uiState.update {
                    it.copy(
                        errorMessage = R.string.failed, isLoading = false
                    )
                }
            }
        }
    }

}