package wtf.test.myapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import wtf.test.myapplication.data.models.Product

/**
 * The Data Access Object for the Product class.
 */
@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProduct(productId: String): Product

    @Query("SELECT * from products ORDER BY name")
    fun getProductsFlow(): Flow<List<Product>>

    @Query("SELECT * from products WHERE productGroupId = :productGroupId ORDER BY name")
    fun getProductsWithProductGroupId(productGroupId: Int): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>)
}
