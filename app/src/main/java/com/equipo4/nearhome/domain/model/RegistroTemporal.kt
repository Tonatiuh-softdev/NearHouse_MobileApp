package com.equipo4.nearhome.domain.model

object RegistroTemporal {
    var correo: String = ""
    var contrasena: String = ""
    var comprobanteCorreo: String = "" // Token JWT devuelto al verificar correo
    var nombre: String = ""
    var apellidos: String = ""
    var telefono: String = ""
    var tokenFirebase: String = ""     // Token firmado por Firebase al verificar SMS

    fun clear() {
        correo = ""
        contrasena = ""
        comprobanteCorreo = ""
        nombre = ""
        apellidos = ""
        telefono = ""
        tokenFirebase = ""
    }
}