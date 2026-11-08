package com.equipo4.nearhome.data.repository

import android.content.Context
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.remote.AdminUsuariosApi
import com.equipo4.nearhome.data.remote.ErrorDto
import com.equipo4.nearhome.data.remote.SuspenderUsuarioDto
import com.equipo4.nearhome.data.remote.UsuarioDetalleDto
import com.equipo4.nearhome.data.remote.UsuarioListaDto
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.domain.model.User
import com.equipo4.nearhome.domain.model.UserAccountRole
import com.equipo4.nearhome.domain.model.UserAccountStatus
import com.equipo4.nearhome.domain.model.UserDetail
import com.equipo4.nearhome.domain.model.UserPlan
import com.equipo4.nearhome.domain.repository.UserFilters
import com.equipo4.nearhome.domain.repository.UserPage
import com.equipo4.nearhome.domain.repository.UserRepository
import com.equipo4.nearhome.util.AppStrings
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.cancellation.CancellationException

class UsuarioAdminException(mensaje: String) : Exception(mensaje)

/** Foto de una cuenta para el panel. Requiere token: se carga con AdminImageLoader. */
fun fotoUsuarioAdminUrl(userId: String): String =
    "${NetworkModule.BASE_URL}api/v1/admin/usuarios/$userId/foto"

class UserRepositoryApi(context: Context) : UserRepository {

    private val session = SessionManager(context.applicationContext)
    private val gson = Gson()

    private val api: AdminUsuariosApi by lazy {
        NetworkModule.provideRetrofit(NetworkModule.provideOkHttpClient(session))
            .create(AdminUsuariosApi::class.java)
    }

    override suspend fun listar(filtros: UserFilters, pagina: Int): Result<UserPage> =
        withContext(Dispatchers.IO) {
            try {
                val respuesta = api.listar(
                    buscar = filtros.buscar.takeIf { it.isNotBlank() },
                    rol = filtros.rol?.apiName,
                    estatus = filtros.estatus?.name,
                    pagina = pagina
                )
                Result.success(
                    UserPage(
                        totalAccounts = respuesta.totalCuentas,
                        totalResults = respuesta.totalResultados,
                        page = respuesta.pagina,
                        hasMore = respuesta.pagina * respuesta.porPagina < respuesta.totalResultados,
                        users = respuesta.usuarios.map { it.toUser() }
                    )
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(UsuarioAdminException(mensajeDe(e)))
            }
        }

    override suspend fun detalle(userId: String): Result<UserDetail> = withContext(Dispatchers.IO) {
        val id = userId.toIntOrNull()
            ?: return@withContext Result.failure(
                UsuarioAdminException(AppStrings.get(R.string.admin_users_not_found))
            )
        try {
            Result.success(api.detalle(id).toDetail())
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            val mensaje = if (e.code() == 404) {
                AppStrings.get(R.string.admin_users_not_found)
            } else {
                mensajeDe(e)
            }
            Result.failure(UsuarioAdminException(mensaje))
        } catch (e: Exception) {
            Result.failure(UsuarioAdminException(mensajeDe(e)))
        }
    }

    override suspend fun suspender(userId: String, dias: Int?, motivo: String): Result<Unit> =
        accion(userId) { id ->
            // La base exige un motivo: si el administrador lo deja vacío, se manda uno por defecto
            val razon = motivo.trim().ifEmpty { AppStrings.get(R.string.admin_user_block_default_reason) }
            api.suspender(id, SuspenderUsuarioDto(dias, razon))
        }

    override suspend fun levantarSuspension(userId: String): Result<Unit> =
        accion(userId) { id -> api.levantarSuspension(id) }

    override suspend fun eliminar(userId: String): Result<Unit> =
        accion(userId) { id -> api.eliminar(id) }

    /**
     * Ejecuta una acción sobre una cuenta. Retrofit NO lanza excepción con errores 4xx o 5xx:
     * hay que revisar isSuccessful, si no, la app cree que todo salió bien.
     */
    private suspend fun accion(
        userId: String,
        llamada: suspend (Int) -> Response<Unit>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val id = userId.toIntOrNull()
            ?: return@withContext Result.failure(
                UsuarioAdminException(AppStrings.get(R.string.admin_users_not_found))
            )
        try {
            val respuesta = llamada(id)
            if (respuesta.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    UsuarioAdminException(
                        mensajeDeRespuesta(respuesta.code(), respuesta.errorBody()?.string())
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(UsuarioAdminException(mensajeDe(e)))
        }
    }

    /** Mensaje para una respuesta de error del servidor. Usa el texto que manda el API si lo hay. */
    private fun mensajeDeRespuesta(codigo: Int, cuerpo: String?): String {
        when (codigo) {
            401 -> return AppStrings.get(R.string.create_listing_error_session)
            403 -> return AppStrings.get(R.string.admin_users_forbidden)
        }
        val detalle = try {
            gson.fromJson(cuerpo, ErrorDto::class.java)
        } catch (x: Exception) {
            null
        }
        detalle?.detalles?.takeIf { it.isNotEmpty() }?.let { return it.joinToString("\n") }
        detalle?.error?.let { return it }
        return AppStrings.get(R.string.admin_users_error)
    }

    private fun mensajeDe(e: Exception): String = when {
        e is HttpException && e.code() == 401 -> AppStrings.get(R.string.create_listing_error_session)
        e is HttpException && e.code() == 403 -> AppStrings.get(R.string.admin_users_forbidden)
        e is IOException -> AppStrings.get(R.string.create_listing_error_network)
        else -> AppStrings.get(R.string.admin_users_error)
    }
}

private fun UsuarioListaDto.toUser() = User(
    id = id.toString(),
    name = nombre,
    email = correo,
    role = UserAccountRole.fromApi(rol),
    status = UserAccountStatus.fromApi(estatus),
    hasPhoto = tieneFoto
)

private fun UsuarioDetalleDto.toDetail() = UserDetail(
    id = id.toString(),
    name = nombre,
    firstSurname = primerApellido,
    secondSurname = segundoApellido?.takeIf { it.isNotBlank() },
    email = correo,
    phone = telefono,
    role = UserAccountRole.fromApi(rol),
    status = UserAccountStatus.fromApi(estatus),
    registrationDate = formatearFecha(fechaRegistro),
    plan = plan?.let { UserPlan(name = it.nombre, expiresAt = it.vencimiento?.let { v -> formatearFecha(v) }) },
    reviewCount = publicaciones.enRevision,
    publishedCount = publicaciones.publicadas,
    rejectedCount = publicaciones.rechazadas,
    pendingReportsCount = reportesPendientes
)

/** Convierte la fecha ISO del API a algo legible, por ejemplo "6 nov 2026". */
private fun formatearFecha(iso: String): String = runCatching {
    OffsetDateTime.parse(iso).format(DateTimeFormatter.ofPattern("d MMM yyyy", AppStrings.locale()))
}.getOrDefault(iso)