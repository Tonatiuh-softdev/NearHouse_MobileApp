package com.equipo4.nearhome.ui.saved

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo4.nearhome.ui.common.NearHomeBottomBar
import com.equipo4.nearhome.ui.common.PropertyCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.compose.ui.res.stringResource
import com.equipo4.nearhome.R

private val NavyColor = Color(0xFF0B2C4D)
private val BorderColor = Color(0xFFE5E7EB)
private val TextDark = Color(0xFF1F2937)
private val TextGray = Color(0xFF6B7280)
private val EmptyCircle = Color(0xFFF5F5F5)
private val EmptyIcon = Color(0xFF757575)

@Composable
fun SavedScreen(
    viewModel: SavedViewModel = viewModel(),
    onPropertyClick: (String) -> Unit = {},
    onBottomTabSelected: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    // IDs en animación de salida: primero se oculta la tarjeta y luego se quita del repositorio.
    var removing by remember { mutableStateOf(setOf<String>()) }

    Scaffold(
        topBar = { SavedTopBar() },
        bottomBar = { NearHomeBottomBar(selectedTab = 1, onTabSelected = onBottomTabSelected) },
        containerColor = Color.White
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center), color = NavyColor
                )

                uiState.errorMessage != null -> Text(
                    uiState.errorMessage!!, color = TextGray, fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp), textAlign = TextAlign.Center
                )

                uiState.properties.isEmpty() -> EmptySavedState()

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp)
                ) {
                    items(items = uiState.properties, key = { it.id }) { property ->
                        AnimatedVisibility(
                            visible = property.id !in removing,
                            exit = shrinkVertically(tween(300)) + fadeOut(tween(300))
                        ) {
                            Box(modifier = Modifier.padding(bottom = 16.dp)) {
                                PropertyCard(
                                    property = property.copy(isSaved = true), // siempre relleno aquí
                                    onToggleSave = {
                                        removing = removing + property.id
                                        scope.launch {
                                            delay(320)
                                            viewModel.onUnsave(property.id)
                                            removing = removing - property.id
                                        }
                                    },
                                    onClick = { onPropertyClick(property.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedTopBar() {
    Column(modifier = Modifier.fillMaxWidth().background(Color.White).statusBarsPadding()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(stringResource(R.string.saved_properties_title), fontSize = 16.sp, fontWeight = FontWeight.Normal, color = TextDark)
        }
        HorizontalDivider(color = BorderColor)
    }
}

@Composable
private fun EmptySavedState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(180.dp).background(EmptyCircle, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Marcador tachado (bookmark con diagonal), dibujado con Canvas.
            Canvas(modifier = Modifier.size(84.dp)) {
                val w = size.width
                val h = size.height
                val stroke = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                val bookmark = Path().apply {
                    moveTo(w * 0.18f, h * 0.10f)
                    lineTo(w * 0.82f, h * 0.10f)
                    lineTo(w * 0.82f, h * 0.92f)
                    lineTo(w * 0.50f, h * 0.70f)
                    lineTo(w * 0.18f, h * 0.92f)
                    close()
                }
                drawPath(bookmark, EmptyIcon, style = stroke)
                drawLine(Color(0xFFF5F5F5), Offset(w * 0.02f, h * 1.0f), Offset(w * 1.0f, h * 0.0f), strokeWidth = 18.dp.toPx())
                drawLine(EmptyIcon, Offset(w * 0.02f, h * 1.0f), Offset(w * 1.0f, h * 0.0f), strokeWidth = 7.dp.toPx(), cap = StrokeCap.Round)
            }
        }
        Spacer(Modifier.height(28.dp))
        Text(
            stringResource(R.string.saved_empty_title),
            fontSize = 18.sp, color = TextDark, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.saved_empty_subtitle),
            fontSize = 12.sp, color = TextGray, textAlign = TextAlign.Center
        )
    }
}