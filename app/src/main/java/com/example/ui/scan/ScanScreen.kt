package com.example.ui.scan

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TrueScanViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(
    viewModel: TrueScanViewModel,
    onNavigateToResult: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var isFlashOn by remember { mutableStateOf(false) }
    var showManualInput by remember { mutableStateOf(false) }
    var manualBarcode by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("scan_screen")
    ) {
        if (cameraPermissionState.status.isGranted) {
            // Camera scanner
            CameraScannerView(
                isFlashOn = isFlashOn,
                onBarcodeScanned = { barcode ->
                    viewModel.fetchProductDetail(barcode)
                    onNavigateToResult(barcode)
                }
            )

            // Transparent overlay mask over screen showing the scanned target cutout
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val strokeWidthPx = 4.dp.toPx()
                        val rectWidth = size.width * 0.75f
                        val rectHeight = size.width * 0.50f
                        val left = (size.width - rectWidth) / 2
                        val top = (size.height - rectHeight) / 2.3f
                        
                        // Mask path
                        val maskPath = Path().apply {
                            addRect(Rect(0f, 0f, size.width, size.height))
                        }
                        
                        // Transparent viewport cutout
                        val cutPath = Path().apply {
                            addRoundRect(
                                RoundRect(
                                    rect = Rect(left, top, left + rectWidth, top + rectHeight),
                                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                                )
                            )
                        }
                        
                        clipPath(maskPath, clipOp = ClipOp.Difference) {
                            clipPath(cutPath, clipOp = ClipOp.Difference) {
                                drawRect(color = Color.Black.copy(alpha = 0.65f))
                            }
                        }
                    }
            )

            // Neon Scanning Box border framing
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(180.dp)
                        .offset(y = (-40).dp)
                        .border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                ) {
                    Text(
                        text = "ALIGN BARCODE",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                    )
                }
            }

            // Floaters & actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info banner
                Surface(
                    color = Color.Black.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "Scans EAN-13, UPC, Code-128",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Flash toggle
                    FilledIconButton(
                        onClick = { isFlashOn = !isFlashOn },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.65f),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .size(56.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
                            .testTag("flash_toggle")
                    ) {
                        Icon(
                            imageVector = if (isFlashOn) Icons.Default.FlashOff else Icons.Default.FlashOn,
                            contentDescription = "Toggle flashlight"
                        )
                    }

                    // Manual code dialogue launcher
                    LargeFloatingActionButton(
                        onClick = { showManualInput = !showManualInput },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("manual_entry_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = "Manual barcode input"
                        )
                    }
                }
            }

        } else {
            // Permission request state block
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Camera Access Required",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "TrueScan scans food packaging barcodes locally in real-time. Please grant camera permission to begin.",
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                Button(
                    onClick = { cameraPermissionState.launchPermissionRequest() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Grant Permission", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        // Animated Manual input panel sheet at screen bottom
        AnimatedVisibility(
            visible = showManualInput,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Enter Code Manually",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = manualBarcode,
                        onValueChange = { manualBarcode = it.filter { c -> c.isDigit() } },
                        label = { Text("EAN / UPC Barcode") },
                        placeholder = { Text("e.g. 5449000131805") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (manualBarcode.isNotEmpty()) {
                                    viewModel.fetchProductDetail(manualBarcode)
                                    onNavigateToResult(manualBarcode)
                                    showManualInput = false
                                }
                            }
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_barcode_input"),
                        trailingIcon = {
                            if (manualBarcode.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        viewModel.fetchProductDetail(manualBarcode)
                                        onNavigateToResult(manualBarcode)
                                        showManualInput = false
                                    }
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = "Query item")
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = { showManualInput = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}
