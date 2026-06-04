package com.example.data.repository

import com.example.data.local.ProductDao
import com.example.data.local.ProductEntity
import com.example.data.model.Product
import com.example.data.model.ProductAnalyzer
import com.example.data.remote.OpenFoodFactsApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException

class ProductRepository(
    private val productDao: ProductDao,
    private val openFoodFactsApi: OpenFoodFactsApi
) {
    // Flow of search history
    val scanHistory: Flow<List<Product>> = productDao.getAllProductsFlow().map { entities ->
        entities.map { it.toDomain() }
    }

    // Flow of favorites
    val favorites: Flow<List<Product>> = productDao.getFavoriteProductsFlow().map { entities ->
        entities.map { it.toDomain() }
    }

    /**
     * Retrieves detail for a product. Attempts online sync first, falls back to cache if offline.
     */
    suspend fun getProduct(barcode: String, isForceRefresh: Boolean = false): Product {
        val trimmedBarcode = barcode.trim()
        val localEntity = productDao.getProductByBarcode(trimmedBarcode)

        if (!isForceRefresh && localEntity != null) {
            // Already cached, update access timestamp
            val updated = localEntity.copy(scannedAt = System.currentTimeMillis())
            productDao.insertProduct(updated)
            return updated.toDomain()
        }

        try {
            val response = openFoodFactsApi.getProductDetails(trimmedBarcode)
            val offProduct = response.product
            
            if (response.status == 1 && offProduct != null) {
                val isFav = localEntity?.isFavorite ?: false
                val domainProduct = ProductAnalyzer.mapToDomainProduct(
                    barcode = response.code ?: trimmedBarcode,
                    offProduct = offProduct,
                    isFavorite = isFav
                )
                // Cache locally
                productDao.insertProduct(ProductEntity.fromDomain(domainProduct))
                return domainProduct
            } else {
                // Not found or API error, fallback to local cache
                if (localEntity != null) {
                    return localEntity.toDomain()
                }
                throw Exception("Product not found on Open Food Facts database.")
            }
        } catch (e: Exception) {
            // Network failure or timeout, fallback to cache
            if (localEntity != null) {
                return localEntity.toDomain()
            }
            throw IOException("Network error: Unable to connect. Please check your internet connection.", e)
        }
    }

    suspend fun setFavorite(barcode: String, isFavorite: Boolean) {
        productDao.updateFavoriteStatus(barcode, isFavorite)
    }

    suspend fun clearHistory(keepFavorites: Boolean) {
        if (keepFavorites) {
            productDao.clearHistoryKeepFavorites()
        } else {
            productDao.clearAllHistory()
        }
    }

    /**
     * Search products by keyword on Open Food Facts and matches favorite status locally
     */
    suspend fun searchProductsByNameOrBarcode(query: String): List<Product> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()

        // 1. Check if query is a numeric barcode
        if (trimmed.all { it.isDigit() } && trimmed.length in 8..15) {
            try {
                val product = getProduct(trimmed)
                return listOf(product)
            } catch (e: Exception) {
                // If barcode fails, continue to text search
            }
        }

        // 2. Query Remote OFF search API
        try {
            val searchResponse = openFoodFactsApi.searchProducts(trimmed)
            val productsList = searchResponse.products ?: emptyList()
            
            return productsList.mapNotNull { offProduct ->
                val code = offProduct.code ?: return@mapNotNull null
                val localCopy = productDao.getProductByBarcode(code)
                val isFav = localCopy?.isFavorite ?: false
                ProductAnalyzer.mapToDomainProduct(
                    barcode = code,
                    offProduct = offProduct,
                    isFavorite = isFav
                )
            }
        } catch (e: Exception) {
            // 3. Fallback to offline search in our local scan repository
            val cacheText = trimmed.lowercase()
            val entities = productDao.getProductByBarcode(trimmed)?.let { listOf(it) } ?: emptyList()
            if (entities.isNotEmpty()) {
                return entities.map { it.toDomain() }
            }
            // Or return similar name in history
            return emptyList()
        }
    }
}
