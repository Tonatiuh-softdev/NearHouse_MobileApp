package com.equipo4.nearhome.data.remote

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.equipo4.nearhome.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.io.IOException

class GoogleAuthManager(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Obtiene el token de Firebase usando Credential Manager.
     * Retorna Result.success(null) si el usuario canceló la ventana.
     * Retorna Result.success(idToken) si tuvo éxito.
     */
    suspend fun getFirebaseIdToken(): Result<String?> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // 1. Mostrar ventana flotante de Google
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            // 2. Extraer credenciales
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val googleIdToken = googleIdTokenCredential.idToken

            Log.i(TAG, "Google regresó la cuenta; iniciando sesión en Firebase")

            // 3. Iniciar sesión en Firebase temporalmente. Si Firebase no contesta, no se espera para siempre.
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = withTimeout(ESPERA_FIREBASE_MS) {
                firebaseAuth.signInWithCredential(firebaseCredential).await()
            }
            Log.i(TAG, "Firebase aceptó la cuenta; pidiendo el token")

            // 4. Obtener el Token de Firebase (forceRefresh = true para asegurar validez)
            val firebaseIdToken = withTimeout(ESPERA_FIREBASE_MS) {
                authResult.user?.getIdToken(true)?.await()?.token
            }
            Log.i(TAG, "Token de Firebase listo (no se imprime)")

            // 5. REGLA OBLIGATORIA: Cerrar sesión en Firebase de inmediato
            firebaseAuth.signOut()

            Result.success(firebaseIdToken)

        } catch (e: GetCredentialCancellationException) {
            // Regla: Si cierra la ventana sin elegir, no se muestra error
            Result.success(null)
        } catch (e: TimeoutCancellationException) {
            // Firebase no contestó a tiempo
            Log.w(TAG, "Firebase no respondió en ${ESPERA_FIREBASE_MS / 1000} s")
            firebaseAuth.signOut()
            Result.failure(Exception("Google tardó demasiado en responder. Revisa tu conexión a internet e inténtalo de nuevo."))
        } catch (e: IOException) {
            // Error de conexión a internet
            Log.w(TAG, "Sin conexión al iniciar sesión con Google: ${e.javaClass.simpleName}: ${e.message}")
            Result.failure(Exception("No se pudo conectar con el servidor. Revisa tu conexión a internet e inténtalo de nuevo."))
        } catch (e: Exception) {
            // Cualquier otro error: se deja en Logcat cuál fue (los mensajes de Firebase no traen tokens)
            Log.w(TAG, "Falló el inicio de sesión con Google: ${e.javaClass.simpleName}: ${e.message}")
            Result.failure(Exception("No se pudo iniciar sesión con Google. Inténtalo de nuevo."))
        }
    }

    private companion object {
        const val TAG = "GoogleAuth"
        const val ESPERA_FIREBASE_MS = 20_000L
    }
}