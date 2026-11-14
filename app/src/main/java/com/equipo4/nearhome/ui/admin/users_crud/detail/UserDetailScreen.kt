package com.equipo4.nearhome.ui.admin.users_crud.detail

import com.equipo4.nearhome.ui.common.DetailBlocksSkeleton
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo4.nearhome.R
import androidx.compose.ui.platform.LocalContext
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.repository.fotoUsuarioAdminUrl
import com.equipo4.nearhome.domain.model.rolesAdministrables
import com.equipo4.nearhome.domain.model.UserDetail
import com.equipo4.nearhome.ui.admin.users_crud.UserAvatar
import com.equipo4.nearhome.ui.admin.users_crud.UserRoleTag
import com.equipo4.nearhome.ui.admin.users_crud.UserStatusBadge
import com.equipo4.nearhome.domain.model.UserAccountRole

private enum class AccionUsuario { NINGUNA, SUSPENDER, ELIMINAR }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    userId: String,
    onBackClick: () -> Unit = {},
    onViewProfileClick: () -> Unit = {}
) {
    val viewModel: UserDetailViewModel = viewModel(
        key = userId,
        factory = UserDetailViewModelFactory(userId)
    )
    val state by viewModel.state.collectAsState()
    val busy by viewModel.actionBusy.collectAsState()
    val actionError by viewModel.actionError.collectAsState()
    var pantalla by remember { mutableStateOf(AccionUsuario.NINGUNA) }
    val snackbarHostState = remember { SnackbarHostState() }
    val usuario = (state as? UserDetailUiState.Success)?.user
    // Solo se administran cuentas de un nivel menor: el administrador a compradores y vendedores,
    // el superadministrador además a administradores (el API también lo valida)
    val context = LocalContext.current
    val rolesSesion = remember { rolesAdministrables(SessionManager(context).getUserRole()) }

    // El botón atrás del sistema cierra la confirmación abierta, no sale del detalle
    BackHandler(enabled = pantalla != AccionUsuario.NINGUNA) {
        viewModel.limpiarErrorAccion()
        pantalla = AccionUsuario.NINGUNA
    }

    // Errores de "Quitar suspensión" se muestran en un Snackbar
    LaunchedEffect(actionError, pantalla) {
        val mensaje = actionError
        if (mensaje != null && pantalla == AccionUsuario.NINGUNA) {
            snackbarHostState.showSnackbar(mensaje)
            viewModel.limpiarErrorAccion()
        }
    }

    if (usuario != null && pantalla == AccionUsuario.SUSPENDER) {
        SuspendUserScreen(
            user = usuario,
            isSubmitting = busy,
            errorMessage = actionError,
            onBackClick = {
                viewModel.limpiarErrorAccion()
                pantalla = AccionUsuario.NINGUNA
            },
            onConfirm = { motivo ->
                viewModel.suspender(dias = null, motivo = motivo) {
                    pantalla = AccionUsuario.NINGUNA
                }
            }
        )
        return
    }

    if (usuario != null && pantalla == AccionUsuario.ELIMINAR) {
        DeleteUserScreen(
            user = usuario,
            isSubmitting = busy,
            errorMessage = actionError,
            onBackClick = {
                viewModel.limpiarErrorAccion()
                pantalla = AccionUsuario.NINGUNA
            },
            onConfirm = {
                viewModel.eliminar {
                    pantalla = AccionUsuario.NINGUNA
                    onBackClick()
                }
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.admin_user_detail_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (usuario != null && usuario.role in rolesSesion) {
                UserActionsBar(
                    status = usuario.status,
                    onBlock = { pantalla = AccionUsuario.SUSPENDER },
                    onUnsuspend = { viewModel.levantarSuspension { } },
                    onDelete = { pantalla = AccionUsuario.ELIMINAR }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        when (val estado = state) {
            is UserDetailUiState.Loading -> {
                DetailBlocksSkeleton(modifier = Modifier.fillMaxSize().padding(innerPadding))
            }

            is UserDetailUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = estado.mensaje,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = viewModel::cargar) {
                        Text(stringResource(R.string.admin_users_retry))
                    }
                }
            }

            is UserDetailUiState.Success -> {
                DetalleContenido(
                    user = estado.user,
                    modifier = Modifier.padding(innerPadding),
                    onViewProfileClick = onViewProfileClick
                )
            }
        }
    }
}

@Composable
private fun DetalleContenido(
    user: UserDetail,
    modifier: Modifier = Modifier,
    onViewProfileClick: () -> Unit = {}
) {
    val nombreCompleto = listOfNotNull(user.name, user.firstSurname, user.secondSurname).joinToString(" ")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SeccionCard(titulo = null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                UserAvatar(
                    name = nombreCompleto,
                    photoUrl = fotoUsuarioAdminUrl(user.id),
                    size = 64.dp
                )
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = nombreCompleto,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = user.email,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    UserStatusBadge(user.status)
                }
            }

            Spacer(Modifier.height(4.dp))

            // Perfil como lo ven los demás: foto, nombre y anuncios publicados
            OutlinedButton(
                onClick = onViewProfileClick,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.user_profile_view),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        SeccionCard(titulo = stringResource(R.string.admin_user_section_account)) {
            FilaDato(stringResource(R.string.admin_user_label_email), user.email)
            FilaDato(stringResource(R.string.admin_user_label_phone), user.phone)
            FilaDatoContenido(stringResource(R.string.admin_user_label_role)) { UserRoleTag(user.role) }
            FilaDatoContenido(stringResource(R.string.admin_user_label_status)) { UserStatusBadge(user.status) }
            FilaDato(stringResource(R.string.admin_user_label_registered), user.registrationDate)
        }

        SeccionCard(titulo = stringResource(R.string.admin_user_section_name)) {
            FilaDato(stringResource(R.string.admin_user_label_name), user.name)
            FilaDato(stringResource(R.string.admin_user_label_first_surname), user.firstSurname)
            FilaDato(
                stringResource(R.string.admin_user_label_second_surname),
                user.secondSurname ?: "—"
            )
        }

        SeccionCard(titulo = stringResource(R.string.admin_user_section_plan)) {
            val plan = user.plan
            if (plan == null) {
                Text(
                    text = stringResource(R.string.admin_user_no_plan),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                FilaDato(stringResource(R.string.admin_user_label_plan), plan.name)
                FilaDato(stringResource(R.string.admin_user_label_expires), plan.expiresAt ?: "—")
            }
        }

        SeccionCard(titulo = stringResource(R.string.admin_user_section_activity)) {
            FilaDato(stringResource(R.string.admin_user_label_review), user.reviewCount.toString())
            FilaDato(stringResource(R.string.admin_user_label_published), user.publishedCount.toString())
            FilaDato(stringResource(R.string.admin_user_label_rejected), user.rejectedCount.toString())
            FilaDato(stringResource(R.string.admin_user_label_pending_reports), user.pendingReportsCount.toString())
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SeccionCard(
    titulo: String?,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (titulo != null) {
                Text(
                    text = titulo,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            contenido()
        }
    }
}

@Composable
private fun FilaDato(etiqueta: String, valor: String) {
    FilaDatoContenido(etiqueta) {
        Text(
            text = valor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun FilaDatoContenido(etiqueta: String, valor: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiqueta,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        valor()
    }
}