package com.equipo4.nearhome.ui.profile.seller

import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material.icons.outlined.ZoomOut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo4.nearhome.R
import com.equipo4.nearhome.util.PhotoCropper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val MAX_ZOOM = 5f

/**
 * Pantalla para encuadrar la foto de perfil dentro de un círculo.
 * El usuario arrastra y pellizca (o usa el slider) y al confirmar se guarda
 * el recorte cuadrado que encierra al círculo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePhotoCropScreen(
    sourceUri: Uri,
    onCancel: () -> Unit,
    onCropped: (Uri) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    BackHandler { onCancel() }

    // null = cargando; Result.failure = no se pudo abrir
    val bitmapResult by produceState<Result<Bitmap>?>(initialValue = null, sourceUri) {
        value = withContext(Dispatchers.IO) {
            PhotoCropper.cargar(context, sourceUri)?.let { Result.success(it) }
                ?: Result.failure(IllegalStateException("No se pudo decodificar"))
        }
    }
    val bitmap = bitmapResult?.getOrNull()

    var zoom by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var isSaving by remember { mutableStateOf(false) }

    val circlePaddingPx = with(LocalDensity.current) { 28.dp.toPx() }
    val diameter = (minOf(canvasSize.width, canvasSize.height) - 2 * circlePaddingPx).coerceAtLeast(0f)

    // Escala con la que la imagen apenas cubre el círculo (zoom = 1)
    val baseScale = bitmap?.let { diameter / minOf(it.width, it.height) } ?: 1f

    fun clampOffset(raw: Offset, z: Float): Offset {
        val bmp = bitmap ?: return Offset.Zero
        val s = baseScale * z
        val maxX = ((bmp.width * s - diameter) / 2f).coerceAtLeast(0f)
        val maxY = ((bmp.height * s - diameter) / 2f).coerceAtLeast(0f)
        return Offset(raw.x.coerceIn(-maxX, maxX), raw.y.coerceIn(-maxY, maxY))
    }

    fun applyZoom(newZoomRaw: Float, focus: Offset = Offset.Zero, pan: Offset = Offset.Zero) {
        val newZoom = newZoomRaw.coerceIn(1f, MAX_ZOOM)
        val k = newZoom / zoom
        offset = clampOffset((offset - focus) * k + focus + pan, newZoom)
        zoom = newZoom
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.crop_photo_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.edit_profile_close),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.ZoomOut,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = zoom,
                            onValueChange = { applyZoom(it) },
                            valueRange = 1f..MAX_ZOOM,
                            enabled = bitmap != null && !isSaving,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )
                        Icon(
                            imageVector = Icons.Outlined.ZoomIn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onCancel,
                            enabled = !isSaving,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.edit_profile_cancel),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = {
                                val bmp = bitmap ?: return@Button
                                val s = baseScale * zoom
                                val left = (bmp.width * s / 2f - offset.x - diameter / 2f) / s
                                val top = (bmp.height * s / 2f - offset.y - diameter / 2f) / s
                                val side = diameter / s
                                isSaving = true
                                scope.launch {
                                    val result = withContext(Dispatchers.IO) {
                                        PhotoCropper.recortarYGuardar(
                                            context, bmp, RectF(left, top, left + side, top + side)
                                        )
                                    }
                                    isSaving = false
                                    if (result != null) onCropped(result)
                                }
                            },
                            enabled = bitmap != null && !isSaving,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.crop_photo_confirm),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = stringResource(R.string.crop_photo_hint),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clipToBounds()
                    .onSizeChanged { canvasSize = it },
                contentAlignment = Alignment.Center
            ) {
                when {
                    bitmapResult == null -> CircularProgressIndicator()

                    bitmap == null -> Text(
                        text = stringResource(R.string.crop_photo_error),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )

                    else -> {
                        val image = remember(bitmap) { bitmap.asImageBitmap() }
                        val scrimColor = MaterialTheme.colorScheme.background.copy(alpha = 0.78f)
                        val ringColor = MaterialTheme.colorScheme.primary

                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(bitmap, canvasSize) {
                                    detectTransformGestures { centroid, pan, gestureZoom, _ ->
                                        if (isSaving) return@detectTransformGestures
                                        val focus = centroid - Offset(size.width / 2f, size.height / 2f)
                                        applyZoom(zoom * gestureZoom, focus, pan)
                                    }
                                }
                                .pointerInput(bitmap) {
                                    // Doble toque: regresar al encuadre inicial
                                    detectTapGestures(onDoubleTap = {
                                        if (!isSaving) {
                                            zoom = 1f
                                            offset = Offset.Zero
                                        }
                                    })
                                }
                        ) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val s = baseScale * zoom
                            val dispW = bitmap.width * s
                            val dispH = bitmap.height * s

                            drawImage(
                                image = image,
                                dstOffset = IntOffset(
                                    (center.x + offset.x - dispW / 2f).toInt(),
                                    (center.y + offset.y - dispH / 2f).toInt()
                                ),
                                dstSize = IntSize(dispW.toInt(), dispH.toInt()),
                                filterQuality = FilterQuality.High
                            )

                            // Velo alrededor del círculo
                            val radius = diameter / 2f
                            val veil = Path().apply {
                                fillType = PathFillType.EvenOdd
                                addRect(Rect(Offset.Zero, size))
                                addOval(Rect(center, radius))
                            }
                            drawPath(veil, scrimColor)

                            drawCircle(
                                color = ringColor,
                                radius = radius,
                                center = center,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }
                }
            }
        }
    }
}
