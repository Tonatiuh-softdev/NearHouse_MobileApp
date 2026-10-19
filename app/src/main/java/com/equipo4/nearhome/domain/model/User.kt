package com.equipo4.nearhome.domain.model

import androidx.annotation.StringRes

import com.equipo4.nearhome.util.AppStrings
import com.equipo4.nearhome.R

enum class UserAccountRole(@StringRes val labelRes: Int) {
    COMPRADOR(R.string.role_buyer),
    VENDEDOR(R.string.role_seller),
    ADMINISTRADOR(R.string.role_admin),
    SUPER_ADMIN(R.string.role_super_admin);

    val label: String get() = AppStrings.get(labelRes)
}

enum class UserAccountStatus(@StringRes val labelRes: Int) {
    VERIFICADO_ACTIVO(R.string.user_status_verified_active),
    PENDIENTE_VERIFICACION(R.string.user_status_pending_verification),
    REGISTRADO(R.string.user_status_registered),
    BLOQUEADO(R.string.user_status_blocked);

    val label: String get() = AppStrings.get(labelRes)
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: UserAccountRole,
    val status: UserAccountStatus,
    val registrationDate: String,
    val activeListingsCount: Int = 0,
    val draftsCount: Int = 0,
    val avatarRes: Int? = null,
    val isSuperAdmin: Boolean = false
) {
    val initials: String
        get() = name.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercase() }
            .joinToString("")
}