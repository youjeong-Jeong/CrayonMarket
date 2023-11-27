package com.example.crayonmarket.view.main.fragment.profile

import androidx.paging.PagingData
import com.example.crayonmarket.model.UserDetail
import com.example.crayonmarket.view.main.fragment.sale.SaleItemUiState

data class ProfileUiState(
    val salePosts: PagingData<SaleItemUiState> = PagingData.empty(),
    val userDetail: UserDetail? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
