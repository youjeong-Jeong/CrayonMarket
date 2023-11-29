package com.example.crayonmarket.view.main.fragment.sale

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.crayonmarket.R
import com.example.crayonmarket.databinding.ItemSalesBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class SaleViewHolder(
    private val binding: ItemSalesBinding, private val onClickSaleItem: (SaleItemUiState) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    val storageReference = Firebase.storage.reference

    fun bind(uiState: SaleItemUiState) = with(binding) {

        val glide = Glide.with(root)

        val writerReference = uiState.writerProfileImageUrl?.let { storageReference.child(it) }
        val postReference = uiState.imageUrl.let { storageReference.child(it) }

        salesTitleText.text = uiState.title

        nickNameText.text = uiState.writerName

        if (uiState.possibleSale) {
            salesCheckingText.text = root.context.getString(R.string.doing_sale)
            salesCheckingText.setBackgroundColor(root.context.getColor(R.color.md_theme_light_primaryContainer))
        } else {
            salesCheckingText.text = root.context.getString(R.string.done_sale)
            salesCheckingText.setBackgroundColor(root.context.getColor(R.color.md_theme_light_error))
        }

        if (writerReference != null) {
            writerReference.downloadUrl.addOnSuccessListener { uri ->
                glide.load(uri).diskCacheStrategy(DiskCacheStrategy.NONE)
                    .fallback(R.drawable.baseline_person_24).circleCrop().into(userProfileImage)
            }
        } else {
            glide.load(R.drawable.baseline_person_24).circleCrop().into(userProfileImage)
        }

        postReference.downloadUrl.addOnSuccessListener { uri ->
            glide.load(uri).into(imageView)
        }

        salesCost.text = uiState.cost

        beforeDayText.text = uiState.time




        root.setOnClickListener { onClickSaleItem(uiState) }
    }
}