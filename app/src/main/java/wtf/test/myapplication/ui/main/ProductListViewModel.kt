package wtf.test.myapplication.ui.main

import androidx.lifecycle.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import wtf.test.myapplication.ProductRepository
import wtf.test.myapplication.data.models.Others
import wtf.test.myapplication.data.models.Product
import wtf.test.myapplication.data.models.ProductGroup

/**
 * The [ViewModel] for fetching a list of [Product]s.
 *
 * The @ExperimentalCoroutinesApi and @FlowPreview indicate that experimental APIs are being used.
 */
@ExperimentalCoroutinesApi
@FlowPreview
class ProductListViewModel internal constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    /**
     * Request a snackbar to display a string.
     *
     * This variable is private because we don't want to expose [MutableLiveData].
     *
     * MutableLiveData allows anyone to set a value, and [ProductListViewModel] is the only
     * class that should be setting values.
     */
    private val _snackbar = MutableLiveData<String?>()

    /**
     * Request a snackbar to display a string.
     */
    val snackbar: LiveData<String?>
        get() = _snackbar

    private val _spinner = MutableLiveData<Boolean>(false)
    /**
     * Show a loading spinner if true
     */
    val spinner: LiveData<Boolean>
        get() = _spinner

    /**
     * The current productGroup selection (flow version)
     */
    private val productGroupChannel = ConflatedBroadcastChannel<ProductGroup>()

    /**
     * A list of plants that updates based on the current filter (flow version)
     */
    val productsFlow: LiveData<List<Product>> = productGroupChannel.asFlow()
        .flatMapLatest { productGroup ->
            if (productGroup == Others) {
                productRepository.productsFlow
            } else {
                productRepository.getProductsWithProductGroup(productGroup)
            }
        }.asLiveData()

    init {
        clearProductGroupFilter()

        productGroupChannel.asFlow()
            .mapLatest { productGroup ->
                _spinner.value = true
                if (productGroup == Others) {
                    productRepository.tryUpdateRecentProductsCache()
                } else {
                    productRepository.tryUpdateRecentProductsForProductGroupCache(productGroup)
                }
            }
            .onCompletion {  _spinner.value = false }
            .catch { throwable ->  _snackbar.value = throwable.message  }
            .launchIn(viewModelScope)
    }

    /**
     * Filter the list to this product group.
     */
    fun setGrowZoneNumber(num: Int) {
        productGroupChannel.offer(ProductGroup(num))
    }

    /**
     * Clear the current filter of this products list.
     */
    fun clearProductGroupFilter() {
        productGroupChannel.offer(Others)
    }

    /**
     * Return true iff the current list is filtered.
     */
    fun isFiltered() = productGroupChannel.value != Others

    /**
     * Called immediately after the UI shows the snackbar.
     */
    fun onSnackbarShown() {
        _snackbar.value = null
    }
}
