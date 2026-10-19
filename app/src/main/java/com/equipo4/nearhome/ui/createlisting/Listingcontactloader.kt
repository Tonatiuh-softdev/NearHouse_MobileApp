package com.equipo4.nearhome.ui.createlisting

import android.content.Context
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.network.AuthApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.coroutines.cancellation.CancellationException

/** Contacto del propietario que se muestra (solo lectura) en el Paso 3. */
data class ContactoUsuario(
    val nombre: String,
    val whatsapp: String?,
    val correo: String?
)

object ListingContactLoader {

    /**
     * 1) Lee lo que ya hay en la sesión (nombre, correo, teléfono).
     * 2) Si falta el teléfono o el correo, los pide a GET api/v1/usuarios/me y los guarda en la sesión
     *    para no volver a pedirlos.
     */
    suspend fun load(context: Context): ContactoUsuario {
        val session = SessionManager(context.applicationContext)
        var nombre = session.getUserName().orEmpty()
        var correo = session.getUserEmail()
        var telefono = session.getUserPhone()

        if (telefono.isNullOrBlank() || correo.isNullOrBlank()) {
            try {
                val api = NetworkModule.provideRetrofit(NetworkModule.provideOkHttpClient(session))
                    .create(AuthApi::class.java)
                val response = api.me()
                if (response.isSuccessful) {
                    val raw = withContext(Dispatchers.IO) { response.body()?.string() }
                    val root = JSONObject(raw ?: "{}")
                    // El usuario puede venir en la raíz o dentro de "usuario" / "data" / "user"
                    val user = listOf("usuario", "data", "user")
                        .firstNotNullOfOrNull { root.optJSONObject(it) } ?: root

                    val apiPhone = user.firstString("telefono", "whatsapp", "celular", "phone")
                    val apiMail = user.firstString("correo", "email")
                    val fullName = listOf(
                        user.firstString("nombre"),
                        user.firstString("primerApellido"),
                        user.firstString("segundoApellido")
                    ).filterNotNull().joinToString(" ")

                    if (fullName.isNotBlank()) nombre = fullName
                    telefono = telefono.takeUnless { it.isNullOrBlank() } ?: apiPhone
                    correo = correo.takeUnless { it.isNullOrBlank() } ?: apiMail
                    session.saveContact(correo, telefono)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Sin internet o respuesta distinta: se muestra lo que haya en la sesión
            }
        }
        return ContactoUsuario(nombre = nombre, whatsapp = telefono, correo = correo)
    }

    private fun JSONObject.firstString(vararg keys: String): String? =
        keys.firstNotNullOfOrNull { key -> optString(key, "").takeIf { it.isNotBlank() && it != "null" } }
}