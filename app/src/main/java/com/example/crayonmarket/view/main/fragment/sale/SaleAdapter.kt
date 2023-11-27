package com.example.crayonmarket.view.main.fragment.sale

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.example.crayonmarket.databinding.ItemSalesBinding

class SaleAdapter(
    private val onClickSaleItem: (SaleItemUiState) -> Unit
) : PagingDataAdapter<SaleItemUiState, SaleViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemSalesBinding.inflate(layoutInflater, parent, false)
        return SaleViewHolder(
            binding = binding, onClickSaleItem = onClickSaleItem
        )
    }

    override fun onBindViewHolder(holder: SaleViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<SaleItemUiState>() {
            override fun areItemsTheSame(
                oldItem: SaleItemUiState, newItem: SaleItemUiState
            ): Boolean {
                return oldItem.uuid == newItem.uuid
            }

            override fun areContentsTheSame(
                oldItem: SaleItemUiState, newItem: SaleItemUiState
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}