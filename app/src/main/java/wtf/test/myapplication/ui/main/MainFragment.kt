package wtf.test.myapplication.ui.main

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import wtf.test.myapplication.ProductRepository
import wtf.test.myapplication.R
import wtf.test.myapplication.data.models.Product
import wtf.test.myapplication.databinding.MainFragmentBinding
import wtf.test.myapplication.utils.Injector

@FlowPreview
@ExperimentalCoroutinesApi
class MainFragment : Fragment(R.layout.main_fragment) {

    private val viewModel: ProductListViewModel by viewModels {
        Injector.provideProductListViewModelFactory(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        val binding = if (root == null) MainFragmentBinding.inflate(inflater, container, false) else MainFragmentBinding.bind(root)
        context ?: return binding.root


        // show the spinner when [ViewModel.spinner] is true
        viewModel.spinner.observe(viewLifecycleOwner) { show ->
            binding.spinner.visibility = if (show) View.VISIBLE else View.GONE
        }

        // Show a snackbar whenever the [ViewModel.snackbar] is updated a non-null value
        viewModel.snackbar.observe(viewLifecycleOwner) { text ->
            text?.let {
                Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show()
                viewModel.onSnackbarShown()
            }
        }

        val adapter = ProductAdapter()
        binding.plantList.adapter = adapter

        viewModel.productsFlow.observe(viewLifecycleOwner) { plants ->
            adapter.submitList(plants)
        }

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_product_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.filter_group -> {
                updateDataByGroup()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateDataByGroup() {
        with(viewModel) {
            if (isFiltered()) {
                clearProductGroupFilter()
            } else {
                setGrowZoneNumber(9)
            }
        }
    }
}

/**
 * Factory for creating a [ProductListViewModel] with a constructor that takes a [ProductRepository].
 */
class ProductListViewModelFactory(
    private val repository: ProductRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = ProductListViewModel(repository) as T
}

