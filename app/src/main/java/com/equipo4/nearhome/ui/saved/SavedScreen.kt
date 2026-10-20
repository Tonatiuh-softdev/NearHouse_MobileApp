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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo4.nearhome.R
import com.equipo4.nearhome.ui.common.NearHomeBottomBar
import com.equipo4.nearhome.ui.common.PropertyCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SavedScreen(
    viewModel: SavedViewModel = viewModel(),
    onPropertyClick: (String) -> Unit = {},
    onBottomTabSelected: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var removing by remember { mutableStateOf(setOf<String>()) }

    Scaffold(
        topBar = { SavedTopBar() },
        bottomBar = { NearHomeBottomBar(selectedTab = 1, onTabSelected = onBottomTabSelected) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )

                uiState.errorMessage != null -> Text(
                    uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    textAlign = TextAlign.Center
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
                                    property = property.copy(isSaved = true),
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
    Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).statusBarsPadding()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                stringResource(R.string.saved_properties_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun EmptySavedState() {
    val emptyCircleColor = MaterialTheme.colorScheme.surfaceVariant
    val emptyIconColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(180.dp).background(emptyCircleColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
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
                drawPath(bookmark, emptyIconColor, style = stroke)
                drawLine(emptyCircleColor, Offset(w * 0.02f, h * 1.0f), Offset(w * 1.0f, h * 0.0f), strokeWidth = 18.dp.toPx())
                drawLine(emptyIconColor, Offset(w * 0.02f, h * 1.0f), Offset(w * 1.0f, h * 0.0f), strokeWidth = 7.dp.toPx(), cap = StrokeCap.Round)
            }
        }
        Spacer(Modifier.height(28.dp))
        Text(
            stringResource(R.string.saved_empty_title),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.saved_empty_subtitle),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
