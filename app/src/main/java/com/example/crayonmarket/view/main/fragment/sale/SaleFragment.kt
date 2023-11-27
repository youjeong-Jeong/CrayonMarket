package com.example.crayonmarket.view.main.fragment.sale

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.crayonmarket.R
import com.example.crayonmarket.databinding.FragmentSaleBinding
import com.example.crayonmarket.view.common.PagingLoadStateAdapter
import com.example.crayonmarket.view.common.ViewBindingFragment
import com.example.crayonmarket.view.common.addDividerDecoration
import com.example.crayonmarket.view.common.registerObserverForScrollToTop
import com.example.crayonmarket.view.common.setListeners
import com.example.crayonmarket.view.main.fragment.sale.detail.SaleDetailActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class SaleFragment : ViewBindingFragment<FragmentSaleBinding>() {

    private val viewModel: SaleViewModel by activityViewModels()
    private lateinit var launcher: ActivityResultLauncher<Intent>

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSaleBinding
        get() = FragmentSaleBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = SaleAdapter(onClickSaleItem = ::onClickSaleItem)
        viewModel.bind()
        initRecyclerView(adapter)
        initEvent()

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                adapter.refresh()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    updateUi(it, adapter)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.bind()
    }

    private fun initEvent() = with(binding) {
        spinner.adapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.sale_index, android.R.layout.simple_spinner_item
        )

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                viewModel.updateSortType(
                    spinner.getItemAtPosition(position).toString()
                )
                viewModel.bind()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.updatePossible(true)
                viewModel.bind()
            } else {
                viewModel.updatePossible(false)
                viewModel.bind()
            }
        }
    }

    private fun initRecyclerView(adapter: SaleAdapter) = with(binding) {
        recyclerView.adapter =
            adapter.withLoadStateFooter(PagingLoadStateAdapter { adapter.retry() })
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.addDividerDecoration()
        loadState.setListeners(adapter, swipeRefreshLayout)
        adapter.registerObserverForScrollToTop(recyclerView)
    }

    private fun updateUi(uiState: SaleUiState, adapter: SaleAdapter) {
        adapter.submitData(viewLifecycleOwner.lifecycle, uiState.salePosts)

        if (uiState.errorMessage != null) {
            viewModel.errorMessageShown()
            showSnackBar(uiState.errorMessage)
        }

    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun onClickSaleItem(saleItemUiState: SaleItemUiState) {
        val intent = SaleDetailActivity.getIntent(
            requireContext(), saleItemUiState.uuid
        )
        launcher.launch(intent)
    }

}