package com.topingames.topineraser.ui.screens.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.topingames.topineraser.ui.theme.TopinSuccess
import com.topingames.topineraser.ui.theme.TopinSuccessDk
import com.topingames.topineraser.ui.theme.TopinTeal
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun EditorScreen(
    onBack: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var sliderValue by remember { mutableFloatStateOf(uiState.tolerance.toFloat()) }
    val sliderFlow = remember { MutableStateFlow(sliderValue.toInt()) }

    LaunchedEffect(Unit) {
        sliderFlow
            .debounce(400L)
            .distinctUntilChanged()
            .collect { tol -> viewModel.applyTolerance(tol) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Editor",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            if (uiState.resultBitmap != null || uiState.isProcessing) {
                Surface(
                    tonalElevation = 4.dp,
                    shadowElevation = 12.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        // Slider row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Left label
                            Text(
                                text = "🎯",
                                fontSize = 16.sp,
                                modifier = Modifier.width(26.dp)
                            )
                            Slider(
                                value = sliderValue,
                                onValueChange = { v ->
                                    sliderValue = v
                                    sliderFlow.value = v.toInt()
                                },
                                valueRange = 5f..120f,
                                steps = 22,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = TopinTeal,
                                    activeTrackColor = TopinTeal,
                                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                            Text(
                                text = "🔥",
                                fontSize = 16.sp,
                                modifier = Modifier.width(26.dp),
                                textAlign = TextAlign.End
                            )
                        }

                        // Tolerance label below slider
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 26.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Preciso",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Tolerancia: ${sliderValue.toInt()}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TopinTeal
                            )
                            Text(
                                text = "Agresivo",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Save button — green when ready
                        val canSave = !uiState.isSaving && !uiState.isProcessing && uiState.resultBitmap != null
                        Button(
                            onClick = { viewModel.savePng(context) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            enabled = canSave,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TopinSuccess,
                                contentColor   = Color(0xFF052010),
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContentColor   = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation  = if (canSave) 6.dp else 0.dp,
                                pressedElevation  = 2.dp
                            )
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier  = Modifier.size(22.dp),
                                    strokeWidth = 2.5.dp,
                                    color = Color(0xFF052010)
                                )
                            } else {
                                Icon(
                                    Icons.Outlined.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Guardar PNG transparente",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isProcessing -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(68.dp),
                            strokeWidth = 4.dp,
                            color = TopinTeal
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Quitando fondo...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tolerancia: ${uiState.tolerance}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                uiState.error != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "⚠️ Error",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.dismissError(); onBack() }) {
                            Text("Volver")
                        }
                    }
                }

                uiState.resultBitmap != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Image preview with checkerboard background
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Checkerboard — signals transparency
                            CheckerboardBackground(
                                modifier = Modifier.matchParentSize()
                            )
                            Image(
                                bitmap = uiState.resultBitmap!!.asImageBitmap(),
                                contentDescription = "Imagen sin fondo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }

                        // Saved snackbar
                        if (uiState.savedFileName != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = TopinSuccessDk
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("✅", fontSize = 18.sp)
                                    Text(
                                        text = "PNG guardado en Imágenes › TopinEraser",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Fondo a cuadros estilo Photoshop para comunicar transparencia */
@Composable
private fun CheckerboardBackground(modifier: Modifier = Modifier) {
    val light = Color(0xFFCCCCCC)
    val dark  = Color(0xFF999999)
    Canvas(modifier = modifier) {
        val cell = 20.dp.toPx()
        val cols = (size.width  / cell).toInt() + 1
        val rows = (size.height / cell).toInt() + 1
        for (row in 0..rows) {
            for (col in 0..cols) {
                drawRect(
                    color   = if ((row + col) % 2 == 0) light else dark,
                    topLeft = Offset(col * cell, row * cell),
                    size    = Size(cell, cell)
                )
            }
        }
    }
}
