package com.equipo4.nearhome.ui.admin.users_crud.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo4.nearhome.ui.admin.users_crud.UserRoleTag
import com.equipo4.nearhome.ui.admin.users_crud.UserStatusBadge
import com.equipo4.nearhome.ui.theme.NoticeTone
import com.equipo4.nearhome.ui.theme.noticeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    userId: String,
    onBackClick: () -> Unit = {},
    onBlockClick: (String) -> Unit = {},
    onDeleteClick: (String) -> Unit = {}
) {
    val viewModel: UserDetailViewModel = viewModel(
        factory = UserDetailViewModelFactory(userId)
    )
    val user by viewModel.user.collectAsState()

    val nombre by viewModel.nombre.collectAsState()
    val primerApellido by viewModel.primerApellido.collectAsState()
    val segundoApellido by viewModel.segundoApellido.collectAsState()
    val correo by viewModel.correo.collectAsState()
    val telefono by viewModel.telefono.collectAsState()

    val nombreError by viewModel.nombreError.collectAsState()
    val apellidoError by viewModel.apellidoError.collectAsState()
    val correoError by viewModel.correoError.collectAsState()
    val telefonoError by viewModel.telefonoError.collectAsState()

    val showGlobalError by viewModel.showGlobalError.collectAsState()
    val isSavedSuccessfully by viewModel.isSavedSuccessfully.collectAsState()

    val subtitle = when {
        showGlobalError -> "Revisa la información"
        isSavedSuccessfully -> "Información actualizada"
        else -> "Información de la cuenta"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Detalle de usuario",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = subtitle,
                            fontSize = 12.sp,
                            color = if (showGlobalError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.saveChanges {
                                // Guardado exitoso
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = if (showGlobalError) "Reintentar guardar" else "Guardar cambios",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onBlockClick(userId) },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Text(
                                text = "Bloquear / Suspender",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }

                        OutlinedButton(
                            onClick = { onDeleteClick(userId) },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Text(
                                text = "Eliminar cuenta",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (user == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            val currentUser = user!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Aviso rojo de error global
                if (showGlobalError) {
                    val dangerColors = noticeColors(NoticeTone.DANGER)
                    Surface(
                        color = dangerColors.container,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, dangerColors.content.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(dangerColors.content, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Error,
                                    contentDescription = null,
                                    tint = dangerColors.container,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "No se guardaron los cambios",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dangerColors.content
                                )
                                Text(
                                    text = "Revisa los campos marcados e inténtalo de nuevo.",
                                    fontSize = 12.sp,
                                    color = dangerColors.content.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }

                // Aviso verde de éxito
                if (isSavedSuccessfully && !showGlobalError) {
                    val successColors = noticeColors(NoticeTone.SUCCESS)
                    Surface(
                        color = successColors.container,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, successColors.content.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(successColors.content, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = successColors.container,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Cambios guardados",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = successColors.content
                                )
                                Text(
                                    text = "La información de ${currentUser.name} se actualizó correctamente.",
                                    fontSize = 12.sp,
                                    color = successColors.content.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }

                // Tarjeta de usuario principal
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentUser.avatarRes != null) {
                            Image(
                                painter = painterResource(id = currentUser.avatarRes),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser.initials,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.width(16.dp))

                        Column {
                            Text(
                                text = currentUser.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = currentUser.email,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(6.dp))
                            UserStatusBadge(currentUser.status, showBackground = false)
                        }
                    }
                }

                // Franja de Rol, Estatus y Registro
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("ROL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            UserRoleTag(currentUser.role)
                        }
                        Column {
                            Text("ESTATUS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            UserStatusBadge(currentUser.status, showBackground = true)
                        }
                        Column {
                            Text("REGISTRO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Text(currentUser.registrationDate, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                // Formulario editable
                Text("Información personal", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

                // Fila 1: Nombre(s) y Primer apellido
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        val isNomError = nombreError != null
                        val nomContainerColor = if (isNomError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
                        val nomLabelColor = if (isNomError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface

                        Text("Nombre(s)", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = nomLabelColor)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { viewModel.nombre.value = it },
                            isError = isNomError,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isNomError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = if (isNomError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                                focusedContainerColor = nomContainerColor,
                                unfocusedContainerColor = nomContainerColor,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (nombreError != null) {
                            Text(nombreError!!, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        val isApError = apellidoError != null
                        val apContainerColor = if (isApError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
                        val apLabelColor = if (isApError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface

                        Text("Primer apellido", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = apLabelColor)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = primerApellido,
                            onValueChange = { viewModel.primerApellido.value = it },
                            isError = isApError,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isApError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = if (isApError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                                focusedContainerColor = apContainerColor,
                                unfocusedContainerColor = apContainerColor,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (apellidoError != null) {
                            Text(apellidoError!!, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                // Segundo apellido (opcional)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Segundo apellido (opcional)", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = segundoApellido,
                        onValueChange = { viewModel.segundoApellido.value = it },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Correo electrónico
                Column(modifier = Modifier.fillMaxWidth()) {
                    val isEmailError = correoError != null
                    val emailContainerColor = if (isEmailError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
                    val emailLabelColor = if (isEmailError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface

                    Text("Correo electrónico", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = emailLabelColor)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = correo,
                        onValueChange = { viewModel.correo.value = it },
                        isError = isEmailError,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isEmailError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (isEmailError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                            focusedContainerColor = emailContainerColor,
                            unfocusedContainerColor = emailContainerColor,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (correoError != null) {
                        Text(correoError!!, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }
                }

                // Teléfono
                Column(modifier = Modifier.fillMaxWidth()) {
                    val isPhoneError = telefonoError != null
                    val phoneContainerColor = if (isPhoneError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
                    val phoneLabelColor = if (isPhoneError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface

                    Text("Teléfono", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = phoneLabelColor)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { viewModel.telefono.value = it },
                        isError = isPhoneError,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isPhoneError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (isPhoneError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                            focusedContainerColor = phoneContainerColor,
                            unfocusedContainerColor = phoneContainerColor,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (telefonoError != null) {
                        Text(telefonoError!!, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}
