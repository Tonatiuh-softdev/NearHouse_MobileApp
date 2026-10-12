package com.equipo4.nearhome.ui.profile

import androidx.annotation.StringRes
import com.equipo4.nearhome.R

enum class UserRole(val dbValue: String, @StringRes val labelRes: Int) {
    COMPRADOR("Comprador", R.string.role_buyer),
    VENDEDOR("Vendedor", R.string.role_seller),
    ADMIN("Administrador", R.string.role_admin),
    SUPER_ADMIN("Super Administrador", R.string.role_super_admin);

    companion object {
        /** Convierte el texto de la base de datos. Desconocido o vacío = Comprador (el de menos permisos). */
        fun fromDb(value: String?): UserRole {
            val text = value?.trim().orEmpty()
            return entries.firstOrNull { it.dbValue.equals(text, ignoreCase = true) } ?: COMPRADOR
        }
    }
}

/** Idiomas disponibles (el valor se guarda tal cual en [ProfileUiState.language]). */
val LanguageOptions = listOf("Español", "Inglés")

data class ProfileUiState(
    val userName: String = "",
    val role: UserRole = UserRole.COMPRADOR,
    val language: String = "Español",
    val darkModeEnabled: Boolean = false
) {
    val darkModeLabel: String get() = if (darkModeEnabled) "Activado" else "Desactivado"

    // Única fuente de verdad de qué opciones ve cada rol.
    val showMyListings: Boolean get() = true
    val showAccountAdmin: Boolean get() = true
    val showAdminPanel: Boolean
        get() = when (role) {
            UserRole.ADMIN, UserRole.SUPER_ADMIN -> true
            UserRole.COMPRADOR, UserRole.VENDEDOR -> false
        }
}