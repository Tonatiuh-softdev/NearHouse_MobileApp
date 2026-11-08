package com.equipo4.nearhome.ui.publicacion.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.equipo4.nearhome.R
import kotlinx.coroutines.launch

/**
 * Galería de fotos que se desliza. Se usa en el detalle de publicación y en el detalle
 * de anuncios del administrador.
 *
 * [showDots] muestra puntos indicadores abajo (en lugar del contador "1/5").
 * [images] acepta URLs (String) del API o recursos (Int) de los datos de prueba.
 * Al tocar una foto se abre a pantalla completa (FullScreenPhotoViewer) para verla sin recortar.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoGallery(
    images: List<Any>,
    modifier: Modifier = Modifier,
    counterSeparator: String = "/",
    showDots: Boolean = false,
    overlay: @Composable BoxScope.() -> Unit = {}
) {
    // Nunca menos de 1 página: un pager con 0 páginas puede fallar
    val pageCount = images.size.coerceAtLeast(1)
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val scope = rememberCoroutineScope()
    // Página abierta en el visor de pantalla completa (null = cerrado)
    var viewerPage by remember { mutableStateOf<Int?>(null) }
    val openViewerCd = stringResource(R.string.photo_viewer_open_cd)

    Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val image = images.getOrNull(page)
            if (image != null) {
                AsyncImage(
                    model = image,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClickLabel = openViewerCd) { viewerPage = page }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
            }
        }

        // Contador "1/5" (solo si no se usan puntos)
        if (images.isNotEmpty() && !showDots) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1}$counterSeparator${images.size}",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }

        // Puntos indicadores (solo si hay más de una foto)
        if (showDots && images.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                images.indices.forEach { index ->
                    val selected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (selected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (selected) Color.White else Color.White.copy(alpha = 0.5f))
                    )
                }
            }
        }

        // Indicador de que la foto se puede abrir completa
        if (images.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(onClickLabel = openViewerCd) { viewerPage = pagerState.currentPage }
                    .padding(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.OpenInFull,
                    contentDescription = openViewerCd,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        overlay()
    }

    viewerPage?.let { start ->
        FullScreenPhotoViewer(
            images = images,
            initialPage = start,
            onDismiss = { lastPage ->
                viewerPage = null
                scope.launch { pagerState.scrollToPage(lastPage) }
            }
        )
    }
}