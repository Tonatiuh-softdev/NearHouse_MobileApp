package com.equipo4.nearhome.data.remote

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.equipo4.nearhome.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
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

            // 3. Iniciar sesión en Firebase temporalmente
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()

            // 4. Obtener el Token de Firebase
            val firebaseIdToken = authResult.user?.getIdToken(false)?.await()?.token

            // 5. REGLA OBLIGATORIA: Cerrar sesión en Firebase de inmediato
            firebaseAuth.signOut()

            Result.success(firebaseIdToken)

        } catch (e: GetCredentialCancellationException) {
            // Regla: Si cierra la ventana sin elegir, no se muestra error
            Result.success(null)
        } catch (e: IOException) {
            // Error de conexión a internet
            Result.failure(Exception("No se pudo conectar con el servidor. Revisa tu conexión a internet e inténtalo de nuevo."))
        } catch (e: Exception) {
            // Cualquier otro error
            Result.failure(Exception("No se pudo iniciar sesión con Google. Inténtalo de nuevo."))
        }
    }
}