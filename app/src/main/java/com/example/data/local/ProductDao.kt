package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM scanned_products ORDER BY scannedAt DESC")
    fun getAllProductsFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM scanned_products WHERE isFavorite = 1 ORDER BY scannedAt DESC")
    fun getFavoriteProductsFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM scanned_products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Query("UPDATE scanned_products SET isFavorite = :isFavorite WHERE barcode = :barcode")
    suspend fun updateFavoriteStatus(barcode: String, isFavorite: Boolean)

    @Query("DELETE FROM scanned_products WHERE barcode = :barcode")
    suspend fun deleteProduct(barcode: String)

    @Query("DELETE FROM scanned_products")
    suspend fun clearAllHistory()

    @Query("DELETE FROM scanned_products WHERE isFavorite = 0")
    suspend fun clearHistoryKeepFavorites()
}
