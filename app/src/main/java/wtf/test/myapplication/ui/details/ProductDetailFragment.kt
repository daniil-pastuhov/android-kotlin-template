package wtf.test.myapplication.ui.details

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ShareCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import wtf.test.myapplication.ProductRepository
import wtf.test.myapplication.R
import wtf.test.myapplication.databinding.FragmentProductDetailBinding
import wtf.test.myapplication.utils.Injector

/**
 * A fragment representing a single product detail screen.
 */
class ProductDetailFragment : Fragment(R.layout.fragment_product_detail) {

    private val args: ProductDetailFragmentArgs by navArgs()

    private val productDetailViewModel: ProductDetailViewModel by viewModels {
        Injector.provideProductDetailViewModelFactory(requireActivity(), args.productId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = super.onCreateView(inflater, container, savedInstanceState)

        val binding = if (root == null)
            FragmentProductDetailBinding.inflate(inflater, container, false) else FragmentProductDetailBinding.bind(root)

        var isToolbarShown = false

        binding.apply {
            viewModel = productDetailViewModel
            lifecycleOwner = viewLifecycleOwner
            // scroll change listener begins at Y = 0 when image is fully collapsed
            productDetailScrollview.setOnScrollChangeListener(
                NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->

                    // User scrolled past image to height of toolbar and the title text is
                    // underneath the toolbar, so the toolbar should be shown.
                    val shouldShowToolbar = scrollY > toolbar.height

                    // The new state of the toolbar differs from the previous state; update
                    // appbar and toolbar attributes.
                    if (isToolbarShown != shouldShowToolbar) {
                        isToolbarShown = shouldShowToolbar

                        // Use shadow animator to add elevation if toolbar is shown
                        appbar.isActivated = shouldShowToolbar

                        // Show the product name if toolbar is shown
                        toolbarLayout.isTitleEnabled = shouldShowToolbar
                    }
                }
            )

            toolbar.setNavigationOnClickListener { view ->
                view.findNavController().navigateUp()
            }

            toolbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_share -> {
                        createShareIntent()
                        true
                    }
                    else -> false
                }
            }
        }

    setHasOptionsMenu(true)

    return binding.root
}

// Helper function for calling a share functionality.
// Should be used when user presses a share button/menu item.
@Suppress("DEPRECATION")
private fun createShareIntent() {
    val shareText = productDetailViewModel.product.value.let { product ->
        if (product == null) {
            ""
        } else {
            getString(R.string.share_text_product, product.name)
        }
    }
    val shareIntent = ShareCompat.IntentBuilder.from(requireActivity())
        .setText(shareText)
        .setType("text/plain")
        .createChooserIntent()
        .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
    startActivity(shareIntent)
}

}
/**
 * Factory for creating a [ProductDetailViewModel] with a constructor that takes a [ProductRepository].
 */
@FlowPreview
@ExperimentalCoroutinesApi
class ProductDetailViewModelFactory(
    private val repository: ProductRepository,
    private val productId: String
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = ProductDetailViewModel(repository, productId) as T
}
