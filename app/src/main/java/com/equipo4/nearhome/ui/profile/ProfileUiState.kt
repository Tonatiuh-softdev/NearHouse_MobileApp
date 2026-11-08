package com.equipo4.nearhome.ui.profile

import androidx.annotation.StringRes
import com.equipo4.nearhome.R
import com.equipo4.nearhome.util.AppStrings

enum class UserRole(val dbValue: String, @StringRes val labelRes: Int) {
    COMPRADOR("Comprador", R.string.role_buyer),
    VENDEDOR("Vendedor", R.string.role_seller),
    ADMIN("Administrador", R.string.role_admin),
    SUPER_ADMIN("Super Administrador", R.string.role_super_admin);

    companion object {
        fun fromDb(value: String?): UserRole {
            val text = value?.trim().orEmpty()
            return entries.firstOrNull { it.dbValue.equals(text, ignoreCase = true) } ?: COMPRADOR
        }
    }
}

val LanguageOptions = listOf("Español", "English")

data class ProfileUiState(
    val isLoading: Boolean = true, // Controla el estado esqueleto del header
    val userName: String = "",
    val userPhotoUrl: String? = null,
    val role: UserRole = UserRole.COMPRADOR,
    val language: String = "Español",
    val darkModeEnabled: Boolean = false
) {
    val darkModeLabel: String get() = AppStrings.get(if (darkModeEnabled) R.string.dark_mode_on else R.string.dark_mode_off)

    val showMyListings: Boolean get() = true
    val showAccountAdmin: Boolean get() = true
    val showAdminPanel: Boolean
        get() = when (role) {
            UserRole.ADMIN, UserRole.SUPER_ADMIN -> true
            UserRole.COMPRADOR, UserRole.VENDEDOR -> false
        }
}