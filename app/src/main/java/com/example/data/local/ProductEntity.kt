package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.NutrientLevelsColorMap
import com.example.data.model.NutrimentDetails
import com.example.data.model.Product
import com.example.data.model.WatchlistIngredient

@Entity(tableName = "scanned_products")
data class ProductEntity(
    @PrimaryKey val barcode: String,
    val name: String,
    val brand: String,
    val imageUrl: String,
    val ingredientsText: String,
    val allergens: List<String>,
    val countries: String,
    val nutriscore: String,
    val novaGroup: Int,
    val nutrientLevels: NutrientLevelsColorMap?,
    val nutriments: NutrimentDetails?,
    val healthScore: Int,
    val watchlistIngredients: List<WatchlistIngredient>,
    val scannedAt: Long,
    val isFavorite: Boolean
) {
    fun toDomain(): Product {
        return Product(
            barcode = barcode,
            name = name,
            brand = brand,
            imageUrl = imageUrl,
            ingredientsText = ingredientsText,
            allergens = allergens,
            countries = countries,
            nutriscore = nutriscore,
            novaGroup = novaGroup,
            nutrientLevels = nutrientLevels ?: NutrientLevelsColorMap("moderate", "moderate", "moderate", "moderate"),
            nutriments = nutriments ?: NutrimentDetails(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            healthScore = healthScore,
            watchlistIngredients = watchlistIngredients,
            scannedAt = scannedAt,
            isFavorite = isFavorite
        )
    }

    companion object {
        fun fromDomain(product: Product): ProductEntity {
            return ProductEntity(
                barcode = product.barcode,
                name = product.name,
                brand = product.brand,
                imageUrl = product.imageUrl,
                ingredientsText = product.ingredientsText,
                allergens = product.allergens,
                countries = product.countries,
                nutriscore = product.nutriscore,
                novaGroup = product.novaGroup,
                nutrientLevels = product.nutrientLevels,
                nutriments = product.nutriments,
                healthScore = product.healthScore,
                watchlistIngredients = product.watchlistIngredients,
                scannedAt = product.scannedAt,
                isFavorite = product.isFavorite
            )
        }
    }
}
