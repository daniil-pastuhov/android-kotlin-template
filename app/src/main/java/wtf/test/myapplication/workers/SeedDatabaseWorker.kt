package wtf.test.myapplication.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.coroutineScope
import wtf.test.myapplication.data.models.Product
import wtf.test.myapplication.utils.AppDatabase

class SeedDatabaseWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = coroutineScope {
        try {
            applicationContext.assets.open("products.json").use { inputStream ->
                JsonReader(inputStream.reader()).use { jsonReader ->
                    val productType = object : TypeToken<List<Product>>() {}.type
                    val products: List<Product> = Gson().fromJson(jsonReader, productType)

                    val database = AppDatabase.getInstance(applicationContext)
                    database.productDao().insertAll(products)

                    Result.success()
                }
            }
        } catch (ex: Exception) {
            Log.e("SeedDatabaseWorker", "Error seeding database", ex)
            Result.failure()
        }
    }
}