package com.equipo4.nearhome.ui.admin.listings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.Error
import com.equipo4.nearhome.ui.theme.adminAccent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.AdminListing
import com.equipo4.nearhome.domain.model.ListingStatus
import com.equipo4.nearhome.ui.theme.NoticeTone
import com.equipo4.nearhome.ui.theme.listingStatusColors
import com.equipo4.nearhome.ui.theme.noticeColors

/** Etiqueta de estado. status = null se muestra como "Eliminado". */
@Composable
fun ListingStatusTag(status: ListingStatus?, modifier: Modifier = Modifier) {
    val colors = listingStatusColors(status)
    Surface(color = colors.container, shape = RoundedCornerShape(12.dp), modifier = modifier) {
        Text(
            text = status?.label() ?: stringResource(R.string.listing_status_deleted),
            color = colors.content,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ListingThumbnail(listing: AdminListing, size: Dp, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(12.dp)
    val first = listing.images.firstOrNull()
    if (first != null) {
        Image(
            painter = painterResource(first),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.size(size).clip(shape)
        )
    } else {
        Box(modifier.size(size).clip(shape).background(MaterialTheme.colorScheme.surfaceVariant))
    }
}

/** Tarjeta de la lista de anuncios. */
@Composable
fun ListingCard(listing: AdminListing, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.padding(10.dp)) {
            ListingThumbnail(listing, 88.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = listing.type.label(),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    ListingStatusTag(listing.status)
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = listing.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = listing.advertiser.name,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = listing.listingType.label(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = priceText(listing.price, listing.listingType),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.listing_registered_on),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatDateTime(listing.registeredAt),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}
/** Tarjeta resumen del anuncio, igual en las cinco pantallas de confirmación. */
@Composable
fun ListingSummaryCard(listing: AdminListing, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ListingThumbnail(listing, 72.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = listing.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${listing.advertiser.name} · ${listing.id}",
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${listing.listingType.label()} · ${priceText(listing.price, listing.listingType)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                ListingStatusTag(listing.status)
            }
        }
    }
}

/** "Estado actual -> Estado nuevo". to = null significa que el anuncio se elimina. */
@Composable
fun StatusChangeRow(from: ListingStatus, to: ListingStatus?, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.listing_change_title),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.listing_change_current),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    ListingStatusTag(from)
                }
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.listing_change_new),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    ListingStatusTag(to)
                }
            }
        }
    }
}


/**
 * Campo de texto obligatorio con su error. El error aparece cuando el usuario ya interactuó con
 * el campo (escribió o salió de él) y el valor no es válido. Título arriba, recuadro con borde
 * rojo cuando hay error y el mensaje con ícono debajo.
 */
@Composable
fun ReasonField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isValid: Boolean,
    errorText: String,
    modifier: Modifier = Modifier,
    hint: String? = null
) {
    var interacted by rememberSaveable { mutableStateOf(false) }
    var hadFocus by remember { mutableStateOf(false) }
    val showError = interacted && !isValid
    val danger = noticeColors(NoticeTone.DANGER).content
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "$label ",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = {
                interacted = true
                onValueChange(it)
            },
            placeholder = if (hint != null) {
                { Text(hint, fontSize = 14.sp) }
            } else null,
            isError = showError,
            minLines = 4,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                errorContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = adminAccent(),
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                errorBorderColor = danger
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    if (hadFocus && !it.isFocused) interacted = true
                    hadFocus = it.isFocused
                }
        )
        if (showError) {
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Error, contentDescription = null, tint = danger, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(errorText, fontSize = 12.sp, color = danger)
            }
        }
    }
}

/** Aviso con icono (naranja, verde, rojo, azul o gris según el tono). */
@Composable
fun NoticeBanner(
    tone: NoticeTone,
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.Info
) {
    val colors = noticeColors(tone)
    Surface(
        color = colors.container,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, colors.content.copy(alpha = 0.25f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, contentDescription = null, tint = colors.content, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(text = text, color = colors.content, fontSize = 13.sp)
        }
    }
}

@Composable
fun CheckRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(checkedColor = adminAccent())
        )
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

/** Barra fija inferior con botones; sube con el teclado y respeta la barra de navegación. */
@Composable
fun FixedActionBar(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/** Snackbar verde de confirmación. */
@Composable
fun ListingSnackbarHost(state: SnackbarHostState) {
    val colors = noticeColors(NoticeTone.SUCCESS)
    SnackbarHost(state) { data ->
        Snackbar(containerColor = colors.container, contentColor = colors.content) {
            Text(data.visuals.message, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun AvatarInitial(name: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.trim().firstOrNull()?.uppercase() ?: "?",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
