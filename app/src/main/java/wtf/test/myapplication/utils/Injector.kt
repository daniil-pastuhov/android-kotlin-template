package wtf.test.myapplication.utils

import android.content.Context
import androidx.annotation.VisibleForTesting
import wtf.test.myapplication.ProductRepository
import wtf.test.myapplication.data.network.NetworkService
import wtf.test.myapplication.ui.details.ProductDetailViewModelFactory
import wtf.test.myapplication.ui.main.ProductListViewModelFactory

interface ViewModelFactoryProvider {
    fun provideProductListViewModelFactory(context: Context): ProductListViewModelFactory
    fun provideProductDetailViewModelFactory(context: Context, productId: String): ProductDetailViewModelFactory
}

val Injector: ViewModelFactoryProvider
    get() = currentInjector

private object DefaultViewModelProvider: ViewModelFactoryProvider {
    private fun getProductRepository(context: Context): ProductRepository {
        return ProductRepository.getInstance(
            productDao(context),
            dataService()
        )
    }

    private fun dataService() = NetworkService()

    private fun productDao(context: Context) =
        AppDatabase.getInstance(context.applicationContext).productDao()

    override fun provideProductListViewModelFactory(context: Context): ProductListViewModelFactory = ProductListViewModelFactory(getProductRepository(context))

    override fun provideProductDetailViewModelFactory(context: Context, productId: String): ProductDetailViewModelFactory =
        ProductDetailViewModelFactory(getProductRepository(context), productId)


}

private object Lock

@Volatile private var currentInjector: ViewModelFactoryProvider =
    DefaultViewModelProvider


@VisibleForTesting
private fun setInjectorForTesting(injector: ViewModelFactoryProvider?) {
    synchronized(Lock) {
        currentInjector = injector ?: DefaultViewModelProvider
    }
}

@VisibleForTesting
private fun resetInjector() =
    setInjectorForTesting(null)