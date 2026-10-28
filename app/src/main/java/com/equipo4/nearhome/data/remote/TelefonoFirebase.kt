package com.equipo4.nearhome.data.remote

import android.app.Activity
import android.util.Log
import com.equipo4.nearhome.R
import com.equipo4.nearhome.util.AppStrings
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

/**
 * Verificación del teléfono por SMS con Firebase.
 *
 * 1. [enviarCodigo]: Firebase manda el SMS y avisa con codigoEnviado(verificationId, tokenReenvio).
 * 2. [credencialConCodigo]: junta ese verificationId con el código que escribió el usuario.
 * 3. [obtenerToken]: inicia sesión en Firebase con la credencial, saca el token y cierra la sesión
 *    de Firebase (la sesión que importa es la de nuestra API). Ese token se manda a la API
 *    en el registro, y la API revisa con Google qué número se verificó.
 *
 * Nunca imprimas el token en Logcat.
 */
object TelefonoFirebase {

    private const val TAG = "TelefonoFirebase"

    /** Segundos que Firebase espera el SMS antes de permitir reenviarlo. */
    const val ESPERA_SEGUNDOS = 60

    /** Lo que Firebase avisa mientras manda el SMS. */
    interface Eventos {
        fun codigoEnviado(verificationId: String, tokenReenvio: PhoneAuthProvider.ForceResendingToken)

        /** Algunos celulares leen el SMS solos: entonces ya no hace falta escribir el código. */
        fun verificadoAutomaticamente(credencial: PhoneAuthCredential)

        fun error(mensaje: String)
    }

    /**
     * Pide a Firebase que mande el SMS.
     * @param telefono en formato internacional, por ejemplo "+523141234567".
     * @param tokenReenvio null la primera vez; para "Reenviar SMS", el que dio codigoEnviado.
     */
    fun enviarCodigo(
        activity: Activity,
        telefono: String,
        tokenReenvio: PhoneAuthProvider.ForceResendingToken?,
        eventos: Eventos,
    ) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                eventos.codigoEnviado(verificationId, token)
            }

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                eventos.verificadoAutomaticamente(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "No se pudo mandar el SMS", e)
                eventos.error(mensajeAlEnviar(e))
            }
        }

        val opciones = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(telefono)
            .setTimeout(ESPERA_SEGUNDOS.toLong(), TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
        if (tokenReenvio != null) {
            opciones.setForceResendingToken(tokenReenvio)
        }
        PhoneAuthProvider.verifyPhoneNumber(opciones.build())
    }

    /** Junta el verificationId del SMS con el código que escribió el usuario. */
    fun credencialConCodigo(verificationId: String, codigo: String): PhoneAuthCredential =
        PhoneAuthProvider.getCredential(verificationId, codigo)

    /**
     * Inicia sesión en Firebase con la credencial, regresa el token para la API y cierra
     * la sesión de Firebase. Si algo falla (por ejemplo, el código está mal), lanza una
     * excepción con un mensaje listo para mostrarle al usuario.
     */
    suspend fun obtenerToken(credencial: PhoneAuthCredential): String {
        val auth = FirebaseAuth.getInstance()
        try {
            val resultado = auth.signInWithCredential(credencial).await()
            return resultado.user?.getIdToken(true)?.await()?.token
                ?: throw IllegalStateException("Firebase no regresó el token")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo verificar el código", e)
            throw Exception(mensajeAlVerificar(e), e)
        } finally {
            auth.signOut()
        }
    }

    private fun mensajeAlEnviar(e: FirebaseException): String = when (e) {
        is FirebaseTooManyRequestsException -> AppStrings.get(R.string.phone_sms_error_too_many)
        is FirebaseNetworkException -> AppStrings.get(R.string.phone_sms_error_network)
        is FirebaseAuthInvalidCredentialsException -> AppStrings.get(R.string.phone_sms_error_invalid_number)
        else -> AppStrings.get(R.string.phone_sms_error_send)
    }

    private fun mensajeAlVerificar(e: Exception): String {
        val codigoError = (e as? FirebaseAuthException)?.errorCode
        return when {
            codigoError == "ERROR_SESSION_EXPIRED" -> AppStrings.get(R.string.phone_sms_error_expired)
            e is FirebaseAuthInvalidCredentialsException -> AppStrings.get(R.string.signup_otp_error)
            e is FirebaseTooManyRequestsException -> AppStrings.get(R.string.phone_sms_error_too_many)
            e is FirebaseNetworkException -> AppStrings.get(R.string.phone_sms_error_network)
            else -> AppStrings.get(R.string.phone_sms_error_verify)
        }
    }
}
