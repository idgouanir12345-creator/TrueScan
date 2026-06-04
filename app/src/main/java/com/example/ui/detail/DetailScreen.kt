package com.example.ui.detail

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.NutrimentDetails
import com.example.data.model.Product
import com.example.data.model.Severity
import com.example.data.model.WatchlistIngredient
import com.example.ui.ProductUiState
import com.example.ui.TrueScanViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    barcode: String,
    viewModel: TrueScanViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.productState.collectAsState()

    // Query on enter if state doesn't match
    LaunchedEffect(barcode) {
        if (uiState !is ProductUiState.Success || (uiState as? ProductUiState.Success)?.product?.barcode != barcode) {
            viewModel.fetchProductDetail(barcode)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Analysis", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
                actions = {
                    if (uiState is ProductUiState.Success) {
                        val product = (uiState as ProductUiState.Success).product
                        IconButton(
                            onClick = { viewModel.toggleFavorite(product) },
                            modifier = Modifier.testTag("bookmark_button")
                        ) {
                            Icon(
                                imageVector = if (product.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Toggle favorite",
                                tint = if (product.isFavorite) DangerRed else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier.testTag("detail_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is ProductUiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Reading product information...", color = MaterialTheme.colorScheme.onBackground)
                    }
                }
                is ProductUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error icon",
                            tint = DangerRed,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Scan Failed",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        Button(
                            onClick = { viewModel.fetchProductDetail(barcode, forceRefresh = true) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Retry Search", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                is ProductUiState.Success -> {
                    ProductDetailContent(product = state.product)
                }
                else -> {
                    // Idle state
                }
            }
        }
    }
}

@Composable
fun ProductDetailContent(product: Product) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Upper product card with image and details
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Coil Async Image loading
            if (product.imageUrl.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = "Product image of ${product.name}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Fastfood,
                        contentDescription = "Placeholder product image",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = product.brand,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Barcode: ${product.barcode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    text = "Origin: ${product.countries}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

        // Health rating dynamic dashboard block
        HealthRatingSegment(product = product)

        // Warnings / Watchlist block
        WatchlistWarningsSection(watchlist = product.watchlistIngredients)

        // Nutrition Table Section
        NutritionFactsSection(nutriments = product.nutriments)

        // Expander Ingredients Section
        IngredientsTextSection(ingredientsText = product.ingredientsText)

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun HealthRatingSegment(product: Product) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Professional Analysis",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                
                val (scoreLabel, scoreColor, desc) = when {
                    product.healthScore >= 70 -> Triple("Excellent", HealthyGreen, "Excellent choices. High nutritional quality.")
                    product.healthScore >= 40 -> Triple("Moderate", ModerateYellow, "Contains moderate levels of sugars/fats. Consume in moderation.")
                    else -> Triple("Poor Choice", DangerRed, "Ultra-processed product containing high salt, sugar, or harmful additives.")
                }

                Text(
                    text = scoreLabel,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = scoreColor
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Beautiful Health Circle Gauge Dial
            ScoreMeterDial(
                score = product.healthScore,
                size = 84.dp,
                dialColor = when {
                    product.healthScore >= 70 -> HealthyGreen
                    product.healthScore >= 40 -> ModerateYellow
                    else -> DangerRed
                }
            )
        }

        // Nova Food Process Code details & Nutriscore double indices
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TealLightBg.copy(alpha = 0.4f))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Nova Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val novaLabel = when (product.novaGroup) {
                    1 -> "Nova 1: Unprocessed"
                    2 -> "Nova 2: Processed culinary"
                    3 -> "Nova 3: Processed"
                    4 -> "Nova 4: Ultra-processed"
                    else -> "Nova Group ${product.novaGroup}"
                }
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Nova process classification info",
                    tint = if (product.novaGroup == 4) DangerRed else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = novaLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }

            // Nutriscore badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "NutriScore:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(getNutriColor(product.nutriscore))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = product.nutriscore.uppercase(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun ScoreMeterDial(score: Int, size: Dp, dialColor: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size)
    ) {
        // Compose Ring Drawing
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 8.dp.toPx()
            // Gray Background Circle path
            drawCircle(
                color = dialColor.copy(alpha = 0.15f),
                style = Stroke(width = strokeWidth)
            )
            // Progress percentage Path Arc
            drawArc(
                color = dialColor,
                startAngle = -90f,
                sweepAngle = (score * 3.6f),
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = score.toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = dialColor
            )
            Text(
                text = "/100",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun WatchlistWarningsSection(watchlist: List<WatchlistIngredient>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Ingredient watchlist analysis",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (watchlist.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = HealthyGreen.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, HealthyGreen.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Safe checklist icon",
                        tint = HealthyGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "100% Core ingredients match safe database. No risk sweeteners, excess sugars, or controversial additives scanned.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                watchlist.forEach { item ->
                    val (cardBg, iconTint, borderCol) = when (item.severity) {
                        Severity.DANGER -> Triple(DangerRed.copy(alpha = 0.08f), DangerRed, DangerRed.copy(alpha = 0.2f))
                        Severity.WARNING -> Triple(WarningOrange.copy(alpha = 0.08f), WarningOrange, WarningOrange.copy(alpha = 0.2f))
                        Severity.INFO -> Triple(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.dp, borderCol)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (item.severity == Severity.DANGER) Icons.Default.Dangerous else Icons.Default.Warning,
                                        contentDescription = "Alert icon",
                                        tint = iconTint,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(iconTint)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = item.type.name.replace("_", " "),
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NutritionFactsSection(nutriments: NutrimentDetails) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Nutritional values (per 100g)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        ) {
            Column {
                NutritionRow("Energy (Calories)", "${nutriments.energyKcal.toInt()} kcal", true)
                NutritionRow("Total Fats", "${String.format("%.1f", nutriments.fat)} g", false)
                NutritionRow(" Saturated Fats", "${String.format("%.1f", nutriments.saturatedFat)} g", false, indent = true)
                NutritionRow("Carbohydrates", "${String.format("%.1f", nutriments.carbohydrates)} g", false)
                NutritionRow(" Sugars", "${String.format("%.1f", nutriments.sugars)} g", false, indent = true)
                NutritionRow("Proteins", "${String.format("%.1f", nutriments.protein)} g", false)
                NutritionRow("Salt / Sodium", "${String.format("%.2f", nutriments.salt)} g", false)
            }
        }
    }
}

@Composable
fun NutritionRow(label: String, value: String, isHeader: Boolean, indent: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isHeader) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = if (indent) 12.dp else 0.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = if (isHeader) FontWeight.ExtraBold else FontWeight.Bold,
            color = if (isHeader) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
}

@Composable
fun IngredientsTextSection(ingredientsText: String) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Full ingredients list",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            onClick = { expanded = !expanded },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = if (expanded) {
                        ingredientsText.ifEmpty { "Full detailed ingredients text not provided for this item." }
                    } else {
                        val text = ingredientsText.ifEmpty { "Detailed ingredients text not available." }
                        if (text.length > 150) text.take(150) + "..." else text
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (expanded) "Collapsible Ingredients" else "Show Details",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Show expand button",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private fun getNutriColor(grade: String): Color {
    return when (grade.lowercase()) {
        "a" -> Color(0xFF1B5E20)
        "b" -> Color(0xFF4CAF50)
        "c" -> Color(0xFFFBC02D)
        "d" -> Color(0xFFF57C00)
        "e" -> Color(0xFFD32F2F)
        else -> Color.Gray
    }
}
