package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OFFResponse(
    @Json(name = "code") val code: String?,
    @Json(name = "status") val status: Int?,
    @Json(name = "status_verbose") val statusVerbose: String?,
    @Json(name = "product") val product: OFFProduct?
)

@JsonClass(generateAdapter = true)
data class OFFSearchResponse(
    @Json(name = "products") val products: List<OFFProduct>?
)

@JsonClass(generateAdapter = true)
data class OFFProduct(
    @Json(name = "code") val code: String?,
    @Json(name = "product_name") val productName: String?,
    @Json(name = "brands") val brands: String?,
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "ingredients_text") val ingredientsText: String?,
    @Json(name = "allergens") val allergens: String?,
    @Json(name = "countries") val countries: String?,
    @Json(name = "nutriscore_grade") val nutriscoreGrade: String?,
    @Json(name = "nova_group") val novaGroup: Int?,
    @Json(name = "nutrient_levels") val nutrientLevels: OFFNutrientLevels?,
    @Json(name = "nutriments") val nutriments: OFFNutriments?
)

@JsonClass(generateAdapter = true)
data class OFFNutrientLevels(
    @Json(name = "fat") val fat: String?,
    @Json(name = "salt") val salt: String?,
    @Json(name = "saturated-fat") val saturatedFat: String?,
    @Json(name = "sugars") val sugars: String?
)

@JsonClass(generateAdapter = true)
data class OFFNutriments(
    @Json(name = "energy-kcal_100g") val energyKcal100g: Double?,
    @Json(name = "fat_100g") val fat100g: Double?,
    @Json(name = "saturated-fat_100g") val saturatedFat100g: Double?,
    @Json(name = "carbohydrates_100g") val carbohydrates100g: Double?,
    @Json(name = "sugars_100g") val sugars100g: Double?,
    @Json(name = "proteins_100g") val proteins100g: Double?,
    @Json(name = "salt_100g") val salt100g: Double?
)

/**
 * Domain Product Object used by the entire application.
 */
data class Product(
    val barcode: String,
    val name: String,
    val brand: String,
    val imageUrl: String,
    val ingredientsText: String,
    val allergens: List<String>,
    val countries: String,
    val nutriscore: String, // e.g. "a", "b", "c", "d", "e"
    val novaGroup: Int, // e.g. 1, 2, 3, 4
    val nutrientLevels: NutrientLevelsColorMap,
    val nutriments: NutrimentDetails,
    val healthScore: Int,
    val watchlistIngredients: List<WatchlistIngredient>,
    val scannedAt: Long,
    val isFavorite: Boolean
)

data class NutrientLevelsColorMap(
    val fat: String, // "low", "moderate", "high"
    val salt: String,
    val saturatedFat: String,
    val sugars: String
)

data class NutrimentDetails(
    val energyKcal: Double,
    val fat: Double,
    val saturatedFat: Double,
    val carbohydrates: Double,
    val sugars: Double,
    val protein: Double,
    val salt: Double
)

data class WatchlistIngredient(
    val name: String,
    val type: WatchlistType, // HIGH_SUGAR, PALM_OIL, ADDITIVE, SWEETENER
    val severity: Severity, // INFO, WARNING, DANGER
    val description: String
)

enum class WatchlistType {
    HIGH_SUGAR, PALM_OIL, ADDITIVE, ALLERGEN, SWEETENER
}

enum class Severity {
    INFO, WARNING, DANGER
}
