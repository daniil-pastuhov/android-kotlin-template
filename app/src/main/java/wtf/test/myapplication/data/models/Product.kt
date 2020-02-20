package wtf.test.myapplication.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey @ColumnInfo(name = "id") val productId: String,
    val name: String,
    val description: String,
    val productGroupId: Int,
    val imageUrl: String = "",
    var isFavorite: Boolean
) {
    override fun toString() = name
}


inline class ProductGroup(val id: Int)
val Others = ProductGroup(-1)