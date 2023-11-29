package com.example.crayonmarket.view.main.fragment.profile.profileupdate

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.PickVisualMediaRequest
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
import com.example.crayonmarket.databinding.ActivityProfileUpdateBinding
import com.example.crayonmarket.model.UserDetail
import com.example.crayonmarket.view.common.getSerializable
import com.example.crayonmarket.view.login.LoginActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch

class ProfileUpdateActivity : AppCompatActivity() {

    companion object {
        fun getIntent(
            context: Context, userDetail: UserDetail
        ): Intent {
            return Intent(context, ProfileUpdateActivity::class.java).apply {
                putExtra("userDetail", userDetail)
            }
        }
    }


    private lateinit var binding: ActivityProfileUpdateBinding

    private val viewModel: ProfileUpdateViewModel by viewModels()

    private fun Uri.toBitmap(context: Context): Bitmap =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(context.contentResolver, this)
            ) { decoder: ImageDecoder, _: ImageDecoder.ImageInfo?, _: ImageDecoder.Source? ->
                decoder.isMutableRequired = true
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            @Suppress("DEPRECATION") BitmapDrawable(
                context.resources, MediaStore.Images.Media.getBitmap(context.contentResolver, this)
            ).bitmap
        }

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { imageUri ->
            if (imageUri != null) {
                val bitmap = imageUri.toBitmap(this)
                viewModel.updateImageBitmap(bitmap)
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userDetail = intent.getSerializable("userDetail", UserDetail::class.java)
        viewModel.bind(userDetail.name)

        initUi(userDetail)
        initEvent()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::updateUi)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initEvent() = with(binding) {
        backButton.setOnClickListener {
            finish()
        }

        doneButton.setOnClickListener {
            viewModel.sendChangedInfo()
        }
    }

    private fun initUi(userDetail: UserDetail) {
        val storage = Firebase.storage
        val storageReference = storage.reference
        val pathReference = userDetail.profileImageUrl?.let { storageReference.child(it) }


        binding.apply {
            imageView.setOnClickListener {
                onClickImage(userDetail)
            }
            pathReference?.downloadUrl?.addOnSuccessListener { uri ->
                Glide.with(this@ProfileUpdateActivity).load(uri)
                    .fallback(R.drawable.baseline_person_24).circleCrop().into(binding.profileImage)
            }
            userNameEditText.setText(userDetail.name)

            userNameEditText.addTextChangedListener {
                if (it != null) {
                    viewModel.updateName(it.toString())
                }
            }
            birthText.text = getString(R.string.birth_text, userDetail.year, userDetail.month, userDetail.day)
        }
    }


    private fun updateUserImage(bitmap: Bitmap?) {
        Glide.with(this@ProfileUpdateActivity).load(bitmap).fallback(R.drawable.baseline_person_24)
            .circleCrop().into(binding.profileImage)
    }

    private fun updateUi(uiState: ProfileUpdateUiState) {
        binding.doneButton.apply {
            val canSave = viewModel.canSave
            isEnabled = canSave
        }
        if (uiState.isImageChanged) {
            updateUserImage(uiState.selectedImageBitmap)
        }

        if (uiState.successToSave) {
            showSnackBar(getString(R.string.chage_profile))
            finish()
        }
        if (uiState.errorMessage != null) {
            showSnackBar(uiState.errorMessage)
            viewModel.errorMessageShown()
        }
    }

    private fun onClickImage(userDetail: UserDetail) {
        val selectedImage = viewModel.uiState.value.selectedImageBitmap
        val oldProfileImageUrl = userDetail.profileImageUrl
        val isImageChanged = viewModel.uiState.value.isImageChanged

        if (selectedImage == null && (oldProfileImageUrl == null || isImageChanged)) {
            showImagePicker()
        } else {
            MaterialAlertDialogBuilder(this).setItems(R.array.image_options) { _, which ->
                when (which) {
                    0 -> {
                        showImagePicker()
                    }

                    1 -> {
                        viewModel.updateImageBitmap(null)
                    }

                    else -> throw IllegalArgumentException()
                }
            }.create().show()
        }
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showImagePicker() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}