package com.equipo4.nearhome.ui.createlisting

import android.content.Context
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.session.CurrentUserStore
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.network.AuthApi
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
     * 2) Si falta el teléfono o el correo, los pide a GET api/v1/usuarios/me,
     *    actualiza CurrentUserStore y guarda el contacto en la sesión.
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
                val response = CurrentUserStore.refresh(api)
                val user = response.body()
                if (response.isSuccessful && user != null) {
                    val fullName = listOfNotNull(
                        user.nombre,
                        user.primerApellido,
                        user.segundoApellido
                    ).joinToString(" ")

                    if (fullName.isNotBlank()) nombre = fullName
                    if (telefono.isNullOrBlank()) telefono = user.telefono
                    if (correo.isNullOrBlank()) correo = user.correo
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
}