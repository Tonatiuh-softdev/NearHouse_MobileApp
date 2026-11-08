package com.equipo4.nearhome.data.repository

import com.equipo4.nearhome.domain.model.Notificacion
import com.equipo4.nearhome.domain.model.TipoNotificacion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

class NotificacionesRepositoryFalso : NotificacionesRepository {

    private val ahora = System.currentTimeMillis()

    private val listaMock = listOf(
        Notificacion(
            id = "1",
            tipo = TipoNotificacion.RECHAZADA,
            timestampMs = ahora - TimeUnit.MINUTES.toMillis(16),
            leida = false,
            imagenUrl = "https://picsum.photos/200/200?random=1",
            publicacionId = "pub_1",
            tituloPropiedad = "Península de Santiago",
            motivoRechazo = "inapropiado"
        ),
        Notificacion(
            id = "2",
            tipo = TipoNotificacion.APROBADA,
            timestampMs = ahora - TimeUnit.MINUTES.toMillis(16),
            leida = false,
            imagenUrl = "https://picsum.photos/200/200?random=2",
            publicacionId = "pub_2",
            tituloPropiedad = "Península de Santiago"
        ),
        Notificacion(
            id = "3",
            tipo = TipoNotificacion.EN_REVISION,
            timestampMs = ahora - TimeUnit.MINUTES.toMillis(16),
            leida = true,
            imagenUrl = "https://picsum.photos/200/200?random=3",
            publicacionId = "pub_3",
            tituloPropiedad = "Península de Santiago"
        ),
        Notificacion(
            id = "4",
            tipo = TipoNotificacion.PAGO,
            timestampMs = ahora - TimeUnit.HOURS.toMillis(2),
            leida = false,
            nombreUsuario = "Sebastián",
            nombrePlan = "Arrendador Pro"
        ),
        Notificacion(
            id = "5",
            tipo = TipoNotificacion.BIENVENIDA,
            timestampMs = ahora - TimeUnit.HOURS.toMillis(24), // Ayer
            leida = false,
            nombreUsuario = "Sebastián"
        ),
        Notificacion(
            id = "6",
            tipo = TipoNotificacion.NOVEDADES_BUSQUEDA,
            timestampMs = ahora - TimeUnit.HOURS.toMillis(26),
            leida = true,
            imagenUrl = "https://picsum.photos/200/200?random=4",
            publicacionId = "pub_4"
        ),
        Notificacion(
            id = "7",
            tipo = TipoNotificacion.ENCONTRAMOS_ALGO,
            timestampMs = ahora - TimeUnit.DAYS.toMillis(10), // Hace > 7 días
            leida = true,
            imagenUrl = "https://picsum.photos/200/200?random=5",
            publicacionId = "pub_5",
            criterioBusqueda = "Departamentos en Manzanillo"
        ),
        Notificacion(
            id = "8",
            tipo = TipoNotificacion.RECIEN_PUBLICADAS,
            timestampMs = ahora - TimeUnit.DAYS.toMillis(12),
            leida = true,
            imagenUrl = "https://picsum.photos/200/200?random=6",
            publicacionId = "pub_6"
        )
    )

    private val _notificaciones = MutableStateFlow(listaMock)
    private var esVacioForzado = false

    override fun getNotificaciones(): Flow<List<Notificacion>> = _notificaciones.asStateFlow()

    override suspend fun marcarComoLeida(id: String) {
        _notificaciones.value = _notificaciones.value.map { item ->
            if (item.id == id) item.copy(leida = true) else item
        }
    }

    override suspend fun alternarEstadoVacio(forceEmpty: Boolean) {
        esVacioForzado = forceEmpty
        _notificaciones.value = if (forceEmpty) emptyList() else listaMock
    }
}