package com.example.data.model

import java.util.Locale

object ProductAnalyzer {

    fun mapToDomainProduct(
        barcode: String,
        offProduct: OFFProduct,
        scannedAt: Long = System.currentTimeMillis(),
        isFavorite: Boolean = false
    ): Product {
        val name = offProduct.productName ?: "Unknown Product"
        val brand = offProduct.brands ?: "Unknown Brand"
        val imageUrl = offProduct.imageUrl ?: ""
        val ingredientsText = offProduct.ingredientsText ?: ""
        
        // Parse allergens
        val allergens = offProduct.allergens
            ?.split(",")
            ?.map { it.trim().removePrefix("en:").removePrefix("fr:").replaceFirstChar { char -> char.uppercase() } }
            ?.filter { it.isNotEmpty() } ?: emptyList()

        val countries = offProduct.countries ?: "Unknown Location"
        val nutriscore = (offProduct.nutriscoreGrade ?: "c").lowercase(Locale.ROOT)
        val novaGroup = offProduct.novaGroup ?: 3

        // Nutrient Levels
        val fatLevel = offProduct.nutrientLevels?.fat ?: "moderate"
        val saltLevel = offProduct.nutrientLevels?.salt ?: "moderate"
        val satFatLevel = offProduct.nutrientLevels?.saturatedFat ?: "moderate"
        val sugarsLevel = offProduct.nutrientLevels?.sugars ?: "moderate"

        val nutrientLevelsMap = NutrientLevelsColorMap(
            fat = fatLevel,
            salt = saltLevel,
            saturatedFat = satFatLevel,
            sugars = sugarsLevel
        )

        // Nutriments block
        val nutriments = NutrimentDetails(
            energyKcal = offProduct.nutriments?.energyKcal100g ?: 0.0,
            fat = offProduct.nutriments?.fat100g ?: 0.0,
            saturatedFat = offProduct.nutriments?.saturatedFat100g ?: 0.0,
            carbohydrates = offProduct.nutriments?.carbohydrates100g ?: 0.0,
            sugars = offProduct.nutriments?.sugars100g ?: 0.0,
            protein = offProduct.nutriments?.proteins100g ?: 0.0,
            salt = offProduct.nutriments?.salt100g ?: 0.0
        )

        // Local watch list detection
        val watchlist = analyzeWatchlist(ingredientsText, allergens, nutriments)

        // Health Score calculation
        val score = calculateHealthScore(nutriments, nutrientLevelsMap, watchlist, novaGroup)

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
            nutrientLevels = nutrientLevelsMap,
            nutriments = nutriments,
            healthScore = score,
            watchlistIngredients = watchlist,
            scannedAt = scannedAt,
            isFavorite = isFavorite
        )
    }

    private fun analyzeWatchlist(
        ingredientsText: String,
        allergens: List<String>,
        nutriments: NutrimentDetails
    ): List<WatchlistIngredient> {
        val list = mutableListOf<WatchlistIngredient>()
        val textLower = ingredientsText.lowercase(Locale.ROOT)

        // Palm Oil detection
        if (textLower.contains("palm oil") || textLower.contains("palm fat") || 
            textLower.contains("huile de palme") || textLower.contains("huiles de palme")) {
            list.add(
                WatchlistIngredient(
                    name = "Palm Oil",
                    type = WatchlistType.PALM_OIL,
                    severity = Severity.WARNING,
                    description = "Palm oil cultivation often contributes to tropical deforestation and habitat loss."
                )
            )
        }

        // Sodium Nitrite detection
        if (textLower.contains("nitrite") || textLower.contains("e250") || textLower.contains("nitrite de sodium")) {
            list.add(
                WatchlistIngredient(
                    name = "Sodium Nitrite (E250)",
                    type = WatchlistType.ADDITIVE,
                    severity = Severity.DANGER,
                    description = "Preservative commonly used in cured meats. Linked to cell division risks and nitrosamines when heated."
                )
            )
        }

        // Aspartame / Artificial Sweetener detection
        if (textLower.contains("aspartame") || textLower.contains("e951")) {
            list.add(
                WatchlistIngredient(
                    name = "Aspartame (E951)",
                    type = WatchlistType.SWEETENER,
                    severity = Severity.DANGER,
                    description = "Popular artificial sweetener. Can trigger migraines or metabolic symptoms in sensitive individuals."
                )
            )
        } else if (textLower.contains("sucralose") || textLower.contains("e955") || 
                   textLower.contains("acesulfame") || textLower.contains("e950")) {
            list.add(
                WatchlistIngredient(
                    name = "Artificial Sweeteners",
                    type = WatchlistType.SWEETENER,
                    severity = Severity.WARNING,
                    description = "Artificial non-nutritive sweetener used for low-calorie profiles, potentially impacting gut flora health."
                )
            )
        }

        // High fructose corn syrup
        if (textLower.contains("fructose corn syrup") || textLower.contains("isoglucose")) {
            list.add(
                WatchlistIngredient(
                    name = "High Fructose Corn Syrup",
                    type = WatchlistType.HIGH_SUGAR,
                    severity = Severity.WARNING,
                    description = "Highly refined sweetener linked to insulin resistance, liver fat deposition, and inflammation."
                )
            )
        } else if (nutriments.sugars > 22.5) {
            list.add(
                WatchlistIngredient(
                    name = "Excessive Sugar",
                    type = WatchlistType.HIGH_SUGAR,
                    severity = Severity.DANGER,
                    description = "Contains sugar levels exceeding 22.5g per 100g. Correlated with diabetes, obesity, and tooth decay."
                )
            )
        }

        // Add allergens to watchlist
        allergens.forEach { allergen ->
            list.add(
                WatchlistIngredient(
                    name = allergen,
                    type = WatchlistType.ALLERGEN,
                    severity = Severity.DANGER,
                    description = "Critical allergen warning for sensitive users. Always review full ingredients before consumption."
                )
            )
        }

        // Other additives of concern
        if (textLower.contains("carrageenan") || textLower.contains("e407")) {
            list.add(
                WatchlistIngredient(
                    name = "Carrageenan (E407)",
                    type = WatchlistType.ADDITIVE,
                    severity = Severity.WARNING,
                    description = "Thickener extracted from red seaweeds, occasionally blamed for gastrointestinal bloating or inflammation."
                )
            )
        }

        if (textLower.contains("monosodium glutamate") || textLower.contains("glutamate") || textLower.contains("e621")) {
            list.add(
                WatchlistIngredient(
                    name = "Monosodium Glutamate (E621)",
                    type = WatchlistType.ADDITIVE,
                    severity = Severity.WARNING,
                    description = "Strong flavor enhancer which might cause hypersensitive reactions in some consumers."
                )
            )
        }

        return list
    }

    private fun calculateHealthScore(
        nutriments: NutrimentDetails,
        levels: NutrientLevelsColorMap,
        watchlist: List<WatchlistIngredient>,
        novaGroup: Int
    ): Int {
        var score = 100

        // 1. High Sugars penalty
        when (levels.sugars) {
            "high" -> score -= 18
            "moderate" -> score -= 6
        }
        if (nutriments.sugars > 30) score -= 5 // extra sugar penalty

        // 2. High Saturated Fat penalty
        val sFat = levels.saturatedFat
        when (sFat) {
            "high" -> score -= 15
            "moderate" -> score -= 5
        }
        if (nutriments.saturatedFat > 8) score -= 5

        // 3. High Salt penalty
        val salt = levels.salt
        when (salt) {
            "high" -> score -= 20
            "moderate" -> score -= 7
        }
        if (nutriments.salt > 2.0) score -= 5

        // 4. Watchlist additives penalty
        val dangerousCount = watchlist.count { it.severity == Severity.DANGER }
        val warningCount = watchlist.count { it.severity == Severity.WARNING }
        score -= (dangerousCount * 12)
        score -= (warningCount * 6)

        // 5. NOVA Processing level penalty
        when (novaGroup) {
            4 -> score -= 20 // Ultra-processed
            3 -> score -= 8   // Processed
            2 -> score -= 3   // Processed culinary ingredients
        }

        // Bound between 0 and 100
        return score.coerceIn(0, 100)
    }
}
