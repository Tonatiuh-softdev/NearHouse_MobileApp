package com.equipo4.nearhome.ui.createlisting

import com.equipo4.nearhome.network.UsuarioMeDto
import com.equipo4.nearhome.ui.profile.UserRole

enum class DestinoCrearAnuncio { CREAR, PLANES }

/**
 * Decide a dónde va el usuario al tocar "Crear anuncio".
 * - Admin y superadmin: directo a crear.
 * - Vendedor con plan vigente: directo a crear.
 * - Comprador, o vendedor sin plan vigente: a planes.
 */
fun destinoCrearAnuncio(usuario: UsuarioMeDto?): DestinoCrearAnuncio {
    return when (UserRole.fromDb(usuario?.rol)) {
        UserRole.ADMIN, UserRole.SUPER_ADMIN -> DestinoCrearAnuncio.CREAR
        UserRole.VENDEDOR ->
            if (usuario?.plan?.vigente == true) DestinoCrearAnuncio.CREAR
            else DestinoCrearAnuncio.PLANES
        UserRole.COMPRADOR -> DestinoCrearAnuncio.PLANES
    }
}