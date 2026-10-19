package com.equipo4.nearhome.ui.createlisting

import com.equipo4.nearhome.ui.profile.UserRole

/** Solo vendedor, administrador y superadministrador crean anuncios. */
fun canCreateListings(role: String?): Boolean = when (UserRole.fromDb(role)) {
    UserRole.VENDEDOR, UserRole.ADMIN, UserRole.SUPER_ADMIN -> true
    UserRole.COMPRADOR -> false
}