package wtf.test.myapplication.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import wtf.test.myapplication.data.models.Product
import wtf.test.myapplication.data.models.ProductGroup

class NetworkService {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://raw.githubusercontent.com/")
        .client(OkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    suspend fun allProducts(): List<Product> = apiService.getAllProducts()

    suspend fun favoriteProducts(): List<String> = withContext(Dispatchers.Default) {
        apiService.getFavoriteProducts().map { product -> product.name }
    }
}

interface ApiService {
    @GET("daniil-pastuhov/android-kotlin-template/master/app/src/main/assets/products.json")
    suspend fun getAllProducts() : List<Product>

    @GET("daniil-pastuhov/android-kotlin-template/master/app/src/main/assets/favorite_proudcts.json")
    suspend fun getFavoriteProducts() : List<Product>
}