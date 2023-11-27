package com.example.crayonmarket.view.main.fragment.profile.profileupdate

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crayonmarket.firebase.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileUpdateViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUpdateUiState())
    val uiState = _uiState.asStateFlow()

    private var didBound = false
    private lateinit var oldName: String

    private val isChanged
        get() = uiState.value.isImageChanged ||
                oldName != uiState.value.name

    val canSave: Boolean
        get() = uiState.value.name.isNotEmpty() && !uiState.value.isLoading && isChanged

    fun bind(oldName: String) {
        check(!didBound)
        didBound = true
        this.oldName = oldName
        _uiState.update {
            it.copy(name = oldName)
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateImageBitmap(bitmap: Bitmap?) {
        _uiState.update { it.copy(selectedImageBitmap = bitmap, isImageChanged = true) }
    }

    fun sendChangedInfo() {
        if (!isChanged) {
            _uiState.update { it.copy(successToSave = true) }
            return
        }
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val uiStateValue = uiState.value
            val result = UserRepository.updateInfo(
                name = uiStateValue.name,
                profileImage = uiStateValue.selectedImageBitmap,
                isChangedImage = uiStateValue.isImageChanged
            )
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(successToSave = true, isLoading = false)
                }
            } else {
                _uiState.update {
                    it.copy(
                        errorMessage = result.exceptionOrNull()!!.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun errorMessageShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}