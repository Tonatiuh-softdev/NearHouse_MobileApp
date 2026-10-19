package com.equipo4.nearhome.ui.createlisting

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import com.equipo4.nearhome.R
import com.equipo4.nearhome.ui.common.NearHomeBottomBar
import com.equipo4.nearhome.ui.home.filter.ALL_COLONIAS
import com.equipo4.nearhome.ui.theme.NoticeTone
import com.equipo4.nearhome.ui.theme.noticeColors
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import java.io.File

private const val MAX_PHOTOS = 5

private fun Context.isGranted(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

@Composable
fun CreateListingStep2Screen(
    viewModel: CreateListingViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onBottomTabSelected: (Int) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme

    var editing by remember { mutableStateOf(false) }
    var permissionDenied by remember { mutableStateOf(false) }
    var cameraGranted by remember { mutableStateOf(context.isGranted(Manifest.permission.CAMERA)) }
    var startCamera by remember { mutableStateOf(false) }
    var pendingPath by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingTab by remember { mutableStateOf<Int?>(null) }
    var mapTouching by remember { mutableStateOf(false) }
    var coloniaOpen by remember { mutableStateOf(false) }
    var coloniaManual by remember { mutableStateOf(false) }

    LaunchedEffect(editing) { if (!editing) mapTouching = false }

    // Si cambia el punto, la colonia vuelve a depender de la detección automática
    LaunchedEffect(state.latitude, state.longitude) {
        coloniaManual = false
        coloniaOpen = false
    }

    // Si falta calle, número o el geocoder no reconoció la colonia, se piden a mano
    val needsManual = state.hasLocation &&
            (state.street.isBlank() || state.streetNumber.isBlank() || state.locationColonia == null)

    // Tocar otra pestaña con datos capturados pregunta antes de descartar
    val onTab: (Int) -> Unit = { index ->
        if (state.hasData()) pendingTab = index else onBottomTabSelected(index)
    }

    // --- Cámara (TakePicture + FileProvider). Nunca galería. ---
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val path = pendingPath
        pendingPath = null
        if (path != null) {
            if (ok) {
                val first = viewModel.state.value.photos.isEmpty()
                viewModel.addPhoto(path)
                if (first) viewModel.locate(context)   // ubicación GPS del celular en ese momento
            } else {
                File(path).delete()
            }
        }
    }

    fun openCamera() {
        val dir = File(context.filesDir, "listing_photos").apply { mkdirs() }
        val file = File(dir, "foto_${System.currentTimeMillis()}.jpg")
        pendingPath = file.absolutePath
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        cameraLauncher.launch(uri)
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { res ->
        val cameraOk = res[Manifest.permission.CAMERA] == true || context.isGranted(Manifest.permission.CAMERA)
        val locationOk = res[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                res[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
                context.isGranted(Manifest.permission.ACCESS_FINE_LOCATION) ||
                context.isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
        cameraGranted = cameraOk
        if (cameraOk && locationOk) {
            permissionDenied = false
            startCamera = true
        } else {
            permissionDenied = true
        }
    }
    LaunchedEffect(startCamera) {
        if (startCamera) {
            startCamera = false
            openCamera()
        }
    }

    fun hasPermissions(): Boolean =
        context.isGranted(Manifest.permission.CAMERA) &&
                (context.isGranted(Manifest.permission.ACCESS_FINE_LOCATION) ||
                        context.isGranted(Manifest.permission.ACCESS_COARSE_LOCATION))

    val onTakePhoto: () -> Unit = {
        if (hasPermissions()) {
            permissionDenied = false
            cameraGranted = true
            openCamera()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        containerColor = cs.surface,
        topBar = { StepHeader(stringResource(R.string.create_listing_step2_title), step = 2, onBack = onBack) },
        bottomBar = {
            Column(Modifier.background(cs.surface)) {
                // El aviso dice exactamente qué falta para poder continuar
                if (!state.isStep2Valid) {
                    val hint = when {
                        state.photos.isEmpty() -> R.string.create_listing_step2_hint
                        !state.hasLocation -> R.string.create_listing_hint_no_location
                        state.locationColonia == null -> R.string.create_listing_hint_colonia
                        else -> R.string.create_listing_hint_confirm
                    }
                    Text(
                        stringResource(hint),
                        style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
                    )
                }
                Button(
                    onClick = onContinue,
                    enabled = state.isStep2Valid,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(48.dp)
                ) { Text(stringResource(R.string.create_listing_continue), fontWeight = FontWeight.SemiBold) }
                NearHomeBottomBar(selectedTab = 3, onTabSelected = onTab)
            }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding)
                // Mientras se toca el mapa en modo corrección, la pantalla no hace scroll
                .verticalScroll(rememberScrollState(), enabled = !mapTouching)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.create_listing_step2_heading),
                    style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.create_listing_step2_subtitle),
                    style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant
                )
            }

            // --- Fotos ---
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.create_listing_capture_photos),
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.create_listing_photo_counter, state.photos.size, MAX_PHOTOS),
                    style = MaterialTheme.typography.labelMedium, color = cs.primary, fontWeight = FontWeight.SemiBold
                )
            }

            // El aviso solo se muestra mientras la cámara aún no tiene permiso
            if (!cameraGranted) {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(cs.errorContainer).padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Warning, null, tint = cs.error, modifier = Modifier.size(18.dp))
                    Text(
                        stringResource(R.string.create_listing_gallery_disabled),
                        style = MaterialTheme.typography.bodySmall, color = cs.onErrorContainer
                    )
                }
            }

            if (permissionDenied) {
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(cs.surfaceVariant).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        stringResource(R.string.create_listing_permissions_explain),
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedButton(onClick = {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            )
                        )
                    }) { Text(stringResource(R.string.create_listing_open_settings)) }
                }
            }

            // Cuadrícula de 3 columnas: miniaturas + un solo cuadro "Tomar Foto" (desaparece al llegar a 5)
            val slots: List<String?> = state.photos + if (state.photos.size < MAX_PHOTOS) listOf(null) else emptyList()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                slots.chunked(3).forEach { rowItems ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowItems.forEach { path ->
                            Box(Modifier.weight(1f).aspectRatio(1f)) {
                                if (path != null) {
                                    PhotoThumb(
                                        path = path,
                                        isCover = state.photos.indexOf(path) == 0,
                                        onDelete = { viewModel.removePhoto(path) }
                                    )
                                } else {
                                    AddPhotoTile(onClick = onTakePhoto)
                                }
                            }
                        }
                        repeat(3 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }

            // --- Ubicación ---
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.create_listing_auto_location),
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold
                )
                if (state.hasLocation) {
                    val tag = noticeColors(NoticeTone.SUCCESS)
                    Text(
                        stringResource(R.string.create_listing_location_detected),
                        style = MaterialTheme.typography.labelSmall, color = tag.content,
                        modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(tag.container)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Text(
                stringResource(R.string.create_listing_location_note),
                style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
            )

            when {
                state.locationLoading -> Box(
                    Modifier.fillMaxWidth().height(180.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                state.locationError -> Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(cs.surfaceVariant).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        stringResource(R.string.create_listing_location_failed),
                        style = MaterialTheme.typography.bodyMedium, color = cs.error
                    )
                    OutlinedButton(onClick = { viewModel.locate(context) }) {
                        Text(stringResource(R.string.create_listing_retry))
                    }
                }

                state.hasLocation -> LocationMap(
                    lat = state.latitude!!, lng = state.longitude!!, editing = editing,
                    onPinSettled = { la, lo -> viewModel.onPinMoved(context, la, lo) },
                    onTouching = { mapTouching = it }
                )

                else -> Box(
                    Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)).background(cs.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.create_listing_map_placeholder),
                        style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant
                    )
                }
            }

            // --- Dirección ---
            if (state.hasLocation && !state.locationLoading) {
                val success = noticeColors(NoticeTone.SUCCESS)
                val coloniaMissing = state.locationColonia == null
                val showColoniaPicker = coloniaMissing || editing
                val showFields = editing || needsManual
                Column(
                    Modifier.fillMaxWidth()
                        .border(
                            if (state.locationConfirmed) 2.dp else 1.dp,
                            if (state.locationConfirmed) success.content else cs.outlineVariant,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        stringResource(R.string.create_listing_address),
                        style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant
                    )

                    if (showColoniaPicker) {
                        // Se elige a mano solo si el geocoder no la reconoció (o al corregir)
                        DropdownHeader(
                            text = state.locationColonia,
                            placeholder = stringResource(R.string.create_listing_select),
                            expanded = coloniaOpen,
                            isError = coloniaMissing,
                            onClick = { coloniaOpen = !coloniaOpen }
                        )
                        if (coloniaOpen) {
                            SingleChoicePanel(ALL_COLONIAS, state.locationColonia) { c ->
                                viewModel.setLocationColonia(c)
                                coloniaManual = true
                                coloniaOpen = false
                            }
                        }
                        if (coloniaMissing) {
                            Text(
                                stringResource(R.string.create_listing_colonia_not_found),
                                style = MaterialTheme.typography.bodySmall, color = cs.error
                            )
                        } else if (coloniaManual) {
                            Text(
                                stringResource(R.string.create_listing_colonia_manual_warning),
                                style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                            )
                        }
                    } else {
                        // Detectada sola: solo lectura
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.create_listing_colonia),
                                style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant
                            )
                            Text(
                                state.locationColonia.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (showFields) {
                        OutlinedTextField(
                            value = state.street,
                            onValueChange = { viewModel.updateAddress(street = it) },
                            label = { Text(stringResource(R.string.create_listing_street)) },
                            singleLine = true, isError = state.street.isBlank(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = state.streetNumber,
                                onValueChange = { viewModel.updateAddress(number = it) },
                                label = { Text(stringResource(R.string.create_listing_number)) },
                                supportingText = { Text(stringResource(R.string.create_listing_number_hint)) },
                                singleLine = true, isError = state.streetNumber.isBlank(),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = state.postalCode,
                                onValueChange = { viewModel.updateAddress(postalCode = it.filter(Char::isDigit).take(5)) },
                                label = { Text(stringResource(R.string.create_listing_postal_code)) },
                                singleLine = true, modifier = Modifier.weight(1f)
                            )
                        }
                        if (editing) {
                            Text(
                                stringResource(R.string.create_listing_drag_pin),
                                style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                            )
                        }
                    }
                    Text(state.fullAddress, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { viewModel.confirmLocation(); editing = false },
                            enabled = state.street.isNotBlank() &&
                                    state.streetNumber.isNotBlank() &&
                                    state.locationColonia != null,
                            colors = if (state.locationConfirmed) {
                                ButtonDefaults.buttonColors(
                                    containerColor = success.container, contentColor = success.content,
                                    disabledContainerColor = success.container, disabledContentColor = success.content
                                )
                            } else ButtonDefaults.buttonColors(),
                            shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)
                        ) {
                            if (state.locationConfirmed) {
                                Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                            }
                            Text(
                                stringResource(
                                    if (state.locationConfirmed) R.string.create_listing_location_confirmed
                                    else R.string.create_listing_confirm_location
                                )
                            )
                        }
                        OutlinedButton(
                            onClick = { editing = !editing },
                            shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)
                        ) { Text(stringResource(R.string.create_listing_correct)) }
                    }
                }
            }
        }
    }

    if (pendingTab != null) {
        DiscardDialog(
            onConfirm = {
                val tab = pendingTab
                pendingTab = null
                viewModel.reset()
                if (tab != null) onBottomTabSelected(tab)
            },
            onDismiss = { pendingTab = null }
        )
    }
}

@Composable
private fun PhotoThumb(path: String, isCover: Boolean, onDelete: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box(Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))) {
        AsyncImage(
            model = File(path), contentDescription = null,
            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
        )
        if (isCover) {
            Text(
                stringResource(R.string.create_listing_cover),
                style = MaterialTheme.typography.labelSmall, color = cs.onPrimary,
                modifier = Modifier.align(Alignment.BottomStart).padding(6.dp)
                    .clip(RoundedCornerShape(6.dp)).background(cs.primary)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp)
                .clip(CircleShape).background(cs.scrim.copy(alpha = 0.6f))
        ) {
            Icon(
                Icons.Default.Close, stringResource(R.string.create_listing_delete_photo),
                tint = Color.White, modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun AddPhotoTile(onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxSize().dashedBorder(cs.outline).clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.PhotoCamera, null, tint = cs.primary)
        Text(
            stringResource(R.string.create_listing_take_photo),
            style = MaterialTheme.typography.labelMedium, color = cs.primary, fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Mapa de la ubicación.
 * - Normal: pin fijo en la ubicación, mapa sin gestos.
 * - Corrección: el mapa se agranda, el pin queda al centro y el usuario mueve el mapa;
 *   al soltarlo se recalcula la dirección.
 */
@Composable
private fun LocationMap(
    lat: Double,
    lng: Double,
    editing: Boolean,
    onPinSettled: (Double, Double) -> Unit,
    onTouching: (Boolean) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(lat, lng), 17f)
    }
    val currentOnSettled by rememberUpdatedState(onPinSettled)
    val currentOnTouching by rememberUpdatedState(onTouching)

    // Fuera de la corrección, el mapa siempre sigue la ubicación
    LaunchedEffect(lat, lng, editing) {
        if (!editing) cameraState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 17f))
    }

    // En corrección: cuando el usuario termina de mover el mapa, se toma el centro
    LaunchedEffect(editing) {
        if (!editing) return@LaunchedEffect
        var byGesture = false
        snapshotFlow { cameraState.isMoving }.collect { moving ->
            if (moving) {
                byGesture = cameraState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE
            } else if (byGesture) {
                byGesture = false
                val target = cameraState.position.target
                currentOnSettled(target.latitude, target.longitude)
            }
        }
    }

    Box(
        Modifier.fillMaxWidth()
            .height(if (editing) 300.dp else 180.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (editing) Modifier.pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            currentOnTouching(event.changes.any { it.pressed })
                        }
                    }
                } else Modifier
            )
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false, mapToolbarEnabled = false,
                scrollGesturesEnabled = editing, zoomGesturesEnabled = editing,
                rotationGesturesEnabled = false, tiltGesturesEnabled = false
            )
        ) {
            if (!editing) {
                Marker(state = rememberMarkerState(key = "$lat,$lng", position = LatLng(lat, lng)))
            }
        }
        if (editing) {
            // Pin fijo al centro: la punta queda justo en el centro del mapa
            Icon(
                Icons.Default.LocationOn, contentDescription = null, tint = cs.error,
                modifier = Modifier.align(Alignment.Center).offset(y = (-20).dp).size(40.dp)
            )
        }
    }
}

private fun Modifier.dashedBorder(color: Color, radius: Dp = 12.dp): Modifier = drawBehind {
    drawRoundRect(
        color = color,
        style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))),
        cornerRadius = CornerRadius(radius.toPx())
    )
}