package com.equipo4.nearhome.ui.admin.users_crud

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.domain.model.initialsOf

/** Un solo ImageLoader para el panel. Su cliente manda el token de la sesión en cada foto. */
object AdminImageLoader {
    @Volatile
    private var instancia: ImageLoader? = null

    fun get(context: Context): ImageLoader =
        instancia ?: synchronized(this) {
            instancia ?: ImageLoader.Builder(context.applicationContext)
                .okHttpClient(NetworkModule.provideOkHttpClient(SessionManager(context.applicationContext)))
                .build()
                .also { instancia = it }
        }
}

/** Foto de la cuenta, o sus iniciales si no hay foto o no carga. */
@Composable
fun UserAvatar(name: String, photoUrl: String?, size: Dp) {
    val loader = AdminImageLoader.get(LocalContext.current)
    val iniciales = initialsOf(name)
    val texto: @Composable () -> Unit = {
        Text(
            text = iniciales,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.36f).sp
        )
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl == null) {
            texto()
        } else {
            SubcomposeAsyncImage(
                model = photoUrl,
                imageLoader = loader,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { texto() },
                error = { texto() }
            )
        }
    }
}