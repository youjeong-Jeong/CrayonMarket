package com.example.crayonmarket.view.main.fragment.sale.addupdate

import android.net.Uri
import androidx.annotation.StringRes

data class SaleAddUiState(
    val title: String = "",
    val content: String = "",
    val cost: String = "",
    val selectedImage: Uri? = null,
    @StringRes val errorMessage: Int? = null,
    val isCreating: Boolean = true,
    val isLoading: Boolean = false,
    val successToUpload: Boolean = false
) {
    val isInputValid: Boolean
        get() = isTitleValid && isContentValid && isCostValid && isImageValid

    private val isTitleValid: Boolean
        get() {
            return title.isNotBlank()
        }

    private val isContentValid: Boolean
        get() {
            return content.isNotBlank()
        }

    private val isCostValid: Boolean
        get() {
            return cost.isNotBlank() && cost.matches(Regex("-?\\d+"))
        }

    val showTitleError: Boolean
        get() = title.isNotEmpty() && !isTitleValid

    val showContentError: Boolean
        get() = content.isNotEmpty() && !isContentValid

    val showCostError: Boolean
        get() = cost.isNotEmpty() && !isCostValid

    private val isImageValid: Boolean
        get() {
            return selectedImage != null
        }
}