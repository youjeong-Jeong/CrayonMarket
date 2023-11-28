package com.example.crayonmarket.view.main.fragment.sale.addupdate

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.crayonmarket.R
import com.example.crayonmarket.databinding.ActivitySaleAddBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch

class SaleAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySaleAddBinding

    private val viewModel: SaleAddViewModel by viewModels()

    private val fileChooserContract =
        registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri ->
            if (imageUri != null) {
                viewModel.selectImage(imageUri)
            } else if (viewModel.uiState.value.selectedImage == null && viewModel.uiState.value.isCreating) {
                finish()
            }
        }

    companion object {
        fun getIntent(
            context: Context,
            uuId: String,
            content: String,
            image: String,
            title: String,
            cost: String
        ): Intent {
            return Intent(context, SaleAddActivity::class.java).apply {
                putExtra("uuId", uuId)
                putExtra("title", title)
                putExtra("content", content)
                putExtra("cost", cost)
                putExtra("image", image)

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaleAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val postTitle = intent.getStringExtra("title")
        val postContent = intent.getStringExtra("content")
        val postImage = intent.getStringExtra("image")
        val postUuid = intent.getStringExtra("uuId")
        val postCost = intent.getStringExtra("cost")

        val glide = Glide.with(this)
        //val storage: FirebaseStorage = FirebaseStorage.getInstance("gs://crayoung-market.appspot.com")
        val storageReference = Firebase.storage.reference

        if (postTitle != null && postContent != null && postUuid != null && postCost != null && postImage != null) {
            viewModel.changeToEditMode()
            val postReference = postImage.let { storageReference.child(it) }
            postReference.downloadUrl.addOnSuccessListener { uri ->
                glide.load(uri).into(binding.imageView)
            }
            binding.toolbarTitle.text = getString(R.string.post_edit)
            binding.postButton.text = getString(R.string.post_editting)
            binding.content.setText(postContent)
            binding.title.setText(postTitle)
            binding.cost.setText(postCost)

        } else {
            showImagePicker()
        }

        binding.imageView.setOnClickListener {
            showImagePicker()
        }

        binding.title.addTextChangedListener {
            viewModel.updateTitle(it.toString())
        }

        binding.content.addTextChangedListener {
            viewModel.updateContent(it.toString())
        }

        binding.cost.addTextChangedListener {
            viewModel.updateCost(it.toString())
        }

        binding.postButton.setOnClickListener {
            if (!viewModel.uiState.value.isCreating) {
                viewModel.editContent(postUuid!!)
            } else {
                viewModel.uploadSale()
            }
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::updateUi)
            }
        }

    }

    private fun updateUi(uiState: SaleAddUiState) = with(binding) {
        if (uiState.selectedImage != null) {
            imageView.setImageURI(uiState.selectedImage)
        }

        if (uiState.errorMessage != null) {
            showSnackBar(getString(uiState.errorMessage))
        }


        titleInputLayout.apply {
            isErrorEnabled = uiState.showTitleError
            error = if (uiState.showTitleError) {
                context.getString(R.string.title_is_not_empty)
            } else null
        }

        contentInputLayout.apply {
            isErrorEnabled = uiState.showContentError
            error = if (uiState.showContentError) {
                context.getString(R.string.content_is_not_empty)
            } else null
        }

        costInputLayout.apply {
            isErrorEnabled = uiState.showCostError
            error = if (uiState.showCostError) {
                context.getString(R.string.cost_is_onlt_int)
            } else null
        }

        if (uiState.successToUpload) {
            Toast.makeText(this@SaleAddActivity, "게시글 업로드에 성공했습니다.", Toast.LENGTH_LONG).show()
            setResult(RESULT_OK)
            finish()
        }

        binding.postButton.apply {
            isEnabled = uiState.isInputValid && !uiState.isLoading
            alpha = if (uiState.isLoading) 0.5F else 1.0F
        }
    }

    private fun showImagePicker() {
        if (!viewModel.uiState.value.isLoading) {
            fileChooserContract.launch("image/*")
        }
    }

    private fun showSnackBar(message: String) {
        val root = binding.postingRoot
        Snackbar.make(root, message, Snackbar.LENGTH_LONG).show()
    }
}