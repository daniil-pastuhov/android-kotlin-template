package wtf.test.myapplication.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import wtf.test.myapplication.ProductRepository
import wtf.test.myapplication.data.models.Product

/**
 * The ViewModel used in [ProductDetailFragment].
 */
@FlowPreview
@ExperimentalCoroutinesApi
class ProductDetailViewModel constructor(
    productRepository: ProductRepository,
    private val productId: String
) : ViewModel() {

    val product: LiveData<Product> = liveData(viewModelScope.coroutineContext + Dispatchers.IO) { emit(productRepository.getProduct(productId)) }
}
