package com.equipo4.nearhome.domain.model

import androidx.annotation.StringRes
import com.equipo4.nearhome.R
import com.equipo4.nearhome.util.AppStrings

/** Rol de la cuenta. [apiName] es el texto que usa el API en el filtro y en las respuestas. */
enum class UserAccountRole(@StringRes val labelRes: Int, val apiName: String) {
    COMPRADOR(R.string.role_buyer, "COMPRADOR"),
    VENDEDOR(R.string.role_seller, "VENDEDOR"),
    ADMINISTRADOR(R.string.role_admin, "ADMINISTRADOR"),
    SUPER_ADMIN(R.string.role_super_admin, "SUPER_ADMINISTRADOR");

    val label: String get() = AppStrings.get(labelRes)

    companion object {
        fun fromApi(valor: String): UserAccountRole =
            entries.firstOrNull { it.apiName == valor } ?: COMPRADOR
    }
}

/** Estatus que calcula el API. Los nombres coinciden con lo que responde. */
enum class UserAccountStatus(@StringRes val labelRes: Int) {
    ACTIVO(R.string.admin_user_status_active),
    SUSPENDIDO(R.string.admin_user_status_suspended),
    DESACTIVADO(R.string.admin_user_status_deactivated);

    val label: String get() = AppStrings.get(labelRes)

    companion object {
        fun fromApi(valor: String): UserAccountStatus =
            entries.firstOrNull { it.name == valor } ?: ACTIVO
    }
}

/** Una cuenta en la lista. */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserAccountRole,
    val status: UserAccountStatus,
    val hasPhoto: Boolean
)

data class UserPlan(val name: String, val expiresAt: String?)

/** Detalle de una cuenta (solo lectura). */
data class UserDetail(
    val id: String,
    val name: String,
    val firstSurname: String,
    val secondSurname: String?,
    val email: String,
    val phone: String,
    val role: UserAccountRole,
    val status: UserAccountStatus,
    val registrationDate: String,
    val plan: UserPlan?,
    val reviewCount: Int,
    val publishedCount: Int,
    val rejectedCount: Int,
    val pendingReportsCount: Int
)

fun initialsOf(name: String): String =
    name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }