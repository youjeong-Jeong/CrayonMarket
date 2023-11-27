package com.example.crayonmarket.view.main.fragment.profile.profileupdate

import android.graphics.Bitmap

data class ProfileUpdateUiState(
    val name: String = "",
    val selectedImageBitmap: Bitmap? = null,
    val isImageChanged: Boolean = false,
    val successToSave: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)