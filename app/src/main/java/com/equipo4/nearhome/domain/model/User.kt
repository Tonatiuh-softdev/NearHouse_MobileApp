package com.equipo4.nearhome.domain.model

enum class UserAccountRole(val label: String) {
    COMPRADOR("Comprador"),
    VENDEDOR("Vendedor"),
    ADMINISTRADOR("Administrador"),
    SUPER_ADMIN("Super Administrador")
}

enum class UserAccountStatus(val label: String) {
    VERIFICADO_ACTIVO("Verificado · Activo"),
    PENDIENTE_VERIFICACION("Pendiente de verificación"),
    REGISTRADO("Registrado"),
    BLOQUEADO("Bloqueado / Suspendido")
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
