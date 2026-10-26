package com.equipo4.nearhome.data.model

/**
 * Mantiene los datos del registro en memoria RAM mientras el usuario
 * avanza por las diferentes pantallas de la app.
 */
object RegistroTemporal {
    var correo: String? = null
    var contrasena: String? = null
    var comprobanteCorreo: String? = null
    var nombre: String? = null
    var primerApellido: String? = null
    var segundoApellido: String? = null
    var telefono: String? = null

    fun limpiar() {
        correo = null
        contrasena = null
        comprobanteCorreo = null
        nombre = null
        primerApellido = null
        segundoApellido = null
        telefono = null
    }
}