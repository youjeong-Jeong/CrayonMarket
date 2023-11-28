package com.example.crayonmarket.view.main.fragment.sale.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.PopupMenu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.crayonmarket.R
import com.example.crayonmarket.databinding.ActivitySaleDetailBinding
import com.example.crayonmarket.view.main.fragment.sale.addupdate.SaleAddActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch


class SaleDetailActivity : AppCompatActivity() {

    companion object {
        fun getIntent(
            context: Context, postUuId: String
        ): Intent {
            return Intent(context, SaleDetailActivity::class.java).apply {
                putExtra("postUuId", postUuId)
            }
        }
    }

    private val viewModel: SaleDetailViewModel by viewModels()
    private lateinit var binding: ActivitySaleDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaleDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initEvent()

        val postUuId = intent.getStringExtra("postUuId")
        viewModel.bind(postUuId = requireNotNull(postUuId))

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::updateUi)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val postUuId = intent.getStringExtra("postUuId")
        viewModel.bind(postUuId = requireNotNull(postUuId))
    }

    private fun initEvent() = with(binding) {
        backButton.setOnClickListener {
            finish()
        }

        chattingRoomButton.setOnClickListener {
            MaterialAlertDialogBuilder(this@SaleDetailActivity).apply {
                setTitle("채팅방 생성")
                setMessage("채팅방을 생성하시겠습니까")
                setNegativeButton("아니오") { _, _ -> }
                setPositiveButton("예") { _, _ ->
                    viewModel.createChattingRoomAndGoCurrentChattingRoom()
                }
            }.show()
        }

        menuButton.setOnClickListener { view ->
            val popupMenu = PopupMenu(applicationContext, view)
            menuInflater.inflate(R.menu.post_detail_menu, popupMenu.menu)
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.update -> {
                        onClickUpdatePostMenu(viewModel.uiState.value)
                        return@setOnMenuItemClickListener true
                    }

                    R.id.delete -> {
                        onClickDeletePostMenu(viewModel.uiState.value)
                        return@setOnMenuItemClickListener true
                    }

                    R.id.salePossible -> {
                        onClickSalePossibleUpdateMenu(viewModel.uiState.value)
                        return@setOnMenuItemClickListener true
                    }

                    else -> {
                        return@setOnMenuItemClickListener false
                    }
                }
            }
        }

    }

    private fun updateUi(uiState: SaleDetailUiState) = with(binding) {
        val detail = uiState.selectedSaleItem

        //val storage: FirebaseStorage = FirebaseStorage.getInstance("gs://market-6c0a3.appspot.com/")
        val storageReference = Firebase.storage.reference

        if (uiState.errorMessage != null) {
            showSnackBar(uiState.errorMessage)
            viewModel.errorMessageShown()
        }

        if (uiState.isDeleteSuccess) {
            finish()
        }

        if (detail != null) {
            val glide = Glide.with(root)

            val postReference = detail.imageUrl.let { storageReference.child(it) }
            postReference.downloadUrl.addOnSuccessListener { uri ->
                glide.load(uri).into(contentImage)
            }

            if (detail.writerProfileImageUrl != null) {
                val profileReference =
                    detail.writerProfileImageUrl.let { storageReference.child(it) }
                profileReference.downloadUrl.addOnSuccessListener { uri ->
                    glide.load(uri).fallback(R.drawable.baseline_person_24).circleCrop()
                        .into(profileImage)
                }
            } else {
                glide.load(R.drawable.baseline_person_24).circleCrop().into(profileImage)
            }

            nameText.text = detail.writerName

            titleText.text = detail.title

            content.text = detail.content

            daysText.text = detail.time

            costText.text = detail.cost

            requireNotNull(uiState.currentSelectedSaleItemPossible)
            if (uiState.currentSelectedSaleItemPossible) {
                salesCheckingText.text = root.context.getString(R.string.doing_sale)
                salesCheckingText.setBackgroundColor(getColor(R.color.md_theme_light_primaryContainer))
            } else {
                salesCheckingText.text = root.context.getString(R.string.done_sale)
                salesCheckingText.setBackgroundColor(getColor(R.color.md_theme_light_error))
            }


            menuButton.isVisible = detail.isMine

            chattingRoomButton.isVisible = !detail.isMine

        }
    }

    private fun onClickDeletePostMenu(uiState: SaleDetailUiState) {
        MaterialAlertDialogBuilder(this).apply {
            setTitle(getString(R.string.delete_post))
            setMessage(R.string.are_you_sure_you_want_to_delete)
            setNegativeButton(R.string.cancel) { _, _ -> }
            setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteSelectedPost(uiState)
            }
        }.show()
    }

    private fun onClickUpdatePostMenu(uiState: SaleDetailUiState) {
        MaterialAlertDialogBuilder(this).apply {
            setTitle(getString(R.string.update_post))
            setMessage(R.string.are_you_sure_you_want_to_update)
            setNegativeButton(R.string.cancel) { _, _ -> }
            setPositiveButton(R.string.update) { _, _ ->
                navigateToEditActivity(uiState)
            }
        }.show()
    }

    private fun onClickSalePossibleUpdateMenu(uiState: SaleDetailUiState) {
        requireNotNull(uiState.currentSelectedSaleItemPossible)
        if (uiState.currentSelectedSaleItemPossible) {
            MaterialAlertDialogBuilder(this).apply {
                setTitle(getString(R.string.change_sale_state))
                setMessage(R.string.are_you_sure_you_wand_to_sale_complete)
                setNegativeButton(R.string.cancel) { _, _ -> }
                setPositiveButton(R.string.complete) { _, _ ->
                    viewModel.editOnlySalePossible(
                        uiState.selectedSaleItem!!.uuid, uiState.currentSelectedSaleItemPossible
                    )
                }
            }.show()
        } else {
            MaterialAlertDialogBuilder(this).apply {
                setTitle(getString(R.string.change_sale_state))
                setMessage(R.string.are_you_sure_you_wand_to_sale_Ing)
                setNegativeButton(R.string.cancel) { _, _ -> }
                setPositiveButton(R.string.complete) { _, _ ->
                    viewModel.editOnlySalePossible(
                        uiState.selectedSaleItem!!.uuid, uiState.currentSelectedSaleItemPossible
                    )
                }
            }.show()
        }
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun navigateToEditActivity(uiState: SaleDetailUiState) {
        val uuId = uiState.selectedSaleItem!!.uuid
        val title = uiState.selectedSaleItem.title
        val content = uiState.selectedSaleItem.content
        val image = uiState.selectedSaleItem.imageUrl
        val cost = uiState.selectedSaleItem.cost

        val intent = SaleAddActivity.getIntent(
            this@SaleDetailActivity,
            uuId = uuId,
            title = title,
            content = content,
            cost = cost,
            image = image
        )
        startActivity(intent)
    }
}
