package wtf.test.myapplication

import androidx.annotation.AnyThread
import androidx.lifecycle.map
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import wtf.test.myapplication.data.ProductDao
import wtf.test.myapplication.data.models.Others
import wtf.test.myapplication.data.models.Product
import wtf.test.myapplication.data.models.ProductGroup
import wtf.test.myapplication.data.network.NetworkService
import wtf.test.myapplication.utils.CacheOnSuccess

/**
 * Repository module for handling data operations.
 *
 * The @ExperimentalCoroutinesApi and @FlowPreview indicate that experimental APIs are being used.
 */
@ExperimentalCoroutinesApi
@FlowPreview
class ProductRepository private constructor(
    private val productDao: ProductDao,
    private val dataService: NetworkService,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    // Cache for storing the custom sort order
    private var productListFavoriteCache = CacheOnSuccess(onErrorFallback = { listOf<String>() }) {
        dataService.favoriteProducts()
    }

    /**
     * Create a flow that calls a single function
     */
    private val customAssortmentFlow = productListFavoriteCache::getOrAwait.asFlow()

    /**
     * Fetch a list of [Product]s from the database and apply a custom assortment to the list.
     * Returns a [flow] List of Products.
     */
    val productsFlow: Flow<List<Product>>
        get() = productDao.getProductsFlow()

            // When the result of customSortFlow is available, this will combine it with the latest
            // value from the flow above.  Thus, as long as both `products` and `sortOrder`
            // have an initial value (their flow has emitted at least one value), any change
            // to either `products` or `sortOrder` will call `products.applySort(sortOrder)`.
            .combine(customAssortmentFlow) { products, assortment ->
                products.applyCustomAssortment(assortment)
            }

            // Flow allows you to switch the dispatcher the previous transforms run on.
            // Doing so introduces a buffer that the lines above this can write to, which we don't
            // need for this UI use-case that only cares about the latest value.
            //
            // This flowOn is needed to make the [background-thread only] applySort call above
            // run on a background thread.
            .flowOn(defaultDispatcher)

            // We can tell flow to make the buffer "conflated". It removes the buffer from flowOn
            // and only shares the last value, as our UI discards any intermediate values in the
            // buffer.
            .conflate()

    /**
     * Fetch a list of [Product]s from the database that matches a given [ProductGroup] and apply a
     * custom assortment to the list. Returns a [Flow].
     *
     * It differs from [productsFlow] in that it only calls *main-safe* suspend functions in the
     * [map] operator, so it does not need to use [flowOn].
     */
    fun getProductsWithProductGroup(productGroup: ProductGroup): Flow<List<Product>> {
        // A Flow from Room will return each value, just like a LiveData.
        return productDao.getProductsWithProductGroupId(productGroup.id)
            // When a new value is sent from the database, we can transform it using a
            // suspending map function. This allows us to call async code like here
            // where it potentially loads the sort order from network (if not cached)
            //
            // Since all calls in this map are main-safe, flowOn is not needed here.
            // Both Room and Retrofit will run the query on a different thread even though this
            // flow is using the main thread.
            .map { products ->

                // We can make a request for the cached sort order directly here, because map
                // takes a suspend lambda
                //
                // This may trigger a network request if it's not yet cached, but since the network
                // call is main safe, we won't block the main thread (even though this flow executes
                // on Dispatchers.Main).
                val sortOrderFromNetwork = productListFavoriteCache.getOrAwait()

                // The result will be the sorted list with custom sort order applied. Note that this
                // call is also main-safe due to using applyMainSafeSort.
                val nextValue = products.applyMainSafeSort(sortOrderFromNetwork)
                nextValue
            }
    }

    /**
     * A function that filter the list of Products with a given custom assortment.
     */
    private fun List<Product>.applyCustomAssortment(customAssortment: List<String>): List<Product> {
        return filter { product -> customAssortment.contains(product.productId) }
    }

    /**
     * The same sorting function as [applyCustomAssortment], but as a suspend function that can run on any thread
     * (main-safe)
     */
    @AnyThread
    private suspend fun List<Product>.applyMainSafeSort(customSortOrder: List<String>) =
        withContext(defaultDispatcher) {
            this@applyMainSafeSort.applyCustomAssortment(customSortOrder)
        }

    /**
     * Returns true if we should make a network request.
     */
    private suspend fun shouldUpdateProductsCache(productGroup: ProductGroup): Boolean {
        // suspending function, so you can e.g. check the status of the database here
        return true
    }

    /**
     * Update the products cache.
     *
     * This function may decide to avoid making a network requests on every call based on a
     * cache-invalidation policy.
     */
    suspend fun tryUpdateRecentProductsCache() {
        if (shouldUpdateProductsCache(Others)) fetchRecentProducts()
    }

    /**
     * Update the products cache for a specific product group.
     *
     * This function may decide to avoid making a network requests on every call based on a
     * cache-invalidation policy.
     */
    suspend fun tryUpdateRecentProductsForProductGroupCache(productGroup: ProductGroup) {
        if (shouldUpdateProductsCache(productGroup)) fetchProductsForProductGroup(productGroup)
    }

    /**
     * Fetch a new list of products from the network, and append them to [productDao]
     */
    private suspend fun fetchRecentProducts() {
        val products = dataService.allProducts()
        productDao.insertAll(products)
    }

    /**
     * Fetch a list of products for a product group from the network, and append them to [productDao]
     */
    private suspend fun fetchProductsForProductGroup(productGroup: ProductGroup): List<Product> = withContext(defaultDispatcher) {
        val products = dataService.allProducts().filter { it.productGroupId == productGroup.id }
        productDao.insertAll(products)
        products
    }

    companion object {
        // For Singleton instantiation
        @Volatile private var instance: ProductRepository? = null

        fun getInstance(productDao: ProductDao, dataService: NetworkService) =
            instance ?: synchronized(this) {
                instance ?: ProductRepository(productDao, dataService).also { instance = it }
            }
    }
}
