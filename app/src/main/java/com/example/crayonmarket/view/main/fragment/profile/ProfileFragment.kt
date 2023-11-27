package com.example.crayonmarket.view.main.fragment.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.crayonmarket.R
import com.example.crayonmarket.databinding.FragmentProfileBinding
import com.example.crayonmarket.model.UserDetail
import com.example.crayonmarket.view.common.PagingLoadStateAdapter
import com.example.crayonmarket.view.common.ViewBindingFragment
import com.example.crayonmarket.view.common.registerObserverForScrollToTop
import com.example.crayonmarket.view.login.LoginActivity
import com.example.crayonmarket.view.main.MainActivity
import com.example.crayonmarket.view.main.fragment.profile.profileupdate.ProfileUpdateActivity
import com.example.crayonmarket.view.main.fragment.sale.SaleAdapter
import com.example.crayonmarket.view.main.fragment.sale.SaleItemUiState
import com.example.crayonmarket.view.main.fragment.sale.detail.SaleDetailActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class ProfileFragment : ViewBindingFragment<FragmentProfileBinding>() {

    private val viewModel: ProfileViewModel by activityViewModels()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentProfileBinding
        get() = FragmentProfileBinding::inflate

    private lateinit var launcher: ActivityResultLauncher<Intent>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bind()
        val adapter = SaleAdapter(onClickSaleItem = ::onClickSaleItem)
        initEvent()
        initRecyclerView(adapter)

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                viewModel.bind()
                adapter.refresh()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    updateUi(it,adapter)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.bind()
    }

    private fun initEvent() = with(binding) {
        profileUpdateButton.setOnClickListener {
            navigateToUpdate(viewModel.uiState.value.userDetail!!)
        }

        logoutButton.setOnClickListener {
            logOut()
        }
    }

    private fun initRecyclerView(adapter: SaleAdapter) = with(binding) {
        recyclerView.adapter = adapter.withLoadStateFooter(
            PagingLoadStateAdapter { adapter.retry() }
        )
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        adapter.registerObserverForScrollToTop(recyclerView)
    }

    private fun updateUi(uiState: ProfileUiState, adapter: SaleAdapter) = with(binding) {
        adapter.submitData(viewLifecycleOwner.lifecycle, uiState.salePosts)

        val storage: FirebaseStorage = FirebaseStorage.getInstance("gs://market-6c0a3.appspot.com/")
        val storageReference = storage.reference

        if (uiState.errorMessage != null) {
            viewModel.errorMessageShown()
            showSnackBar(uiState.errorMessage)
        }

        if (uiState.userDetail != null) {
            val glide = Glide.with(root)

            if (uiState.userDetail.profileImageUrl != null) {
                val profileReference =
                    uiState.userDetail.profileImageUrl.let { storageReference.child(it) }
                profileReference.downloadUrl.addOnSuccessListener { uri ->
                    glide.load(uri).fallback(R.drawable.baseline_person_24).circleCrop()
                        .into(userProfileImage)
                }
            } else {
                glide.load(R.drawable.baseline_person_24).circleCrop().into(userProfileImage)
            }

            userNameText.text = uiState.userDetail.name
        }

    }

    private fun logOut() {
        Firebase.auth.signOut()
        requireActivity().finish()
        val intent = LoginActivity.getIntent(requireContext())
        startActivity(intent)
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun navigateToUpdate(userDetail: UserDetail) {
        val intent = ProfileUpdateActivity.getIntent(requireContext(), userDetail)
        launcher.launch(intent)
    }

    private fun onClickSaleItem(saleItemUiState: SaleItemUiState) {
        val intent = SaleDetailActivity.getIntent(
            requireContext(), saleItemUiState.uuid
        )
        launcher.launch(intent)
    }
}