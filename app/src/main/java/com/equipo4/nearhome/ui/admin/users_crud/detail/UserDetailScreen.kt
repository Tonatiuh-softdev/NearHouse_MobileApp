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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo4.nearhome.ui.admin.users_crud.UserRoleTag
import com.equipo4.nearhome.ui.admin.users_crud.UserStatusBadge

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
                            color = Color(0xFF1F2937)
                        )
                        Text(
                            text = subtitle,
                            fontSize = 12.sp,
                            color = if (showGlobalError) Color(0xFFB91C1C) else Color(0xFF6B7280)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Color(0xFF1F2937)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B2C4D))
                    ) {
                        Text(
                            text = if (showGlobalError) "Reintentar guardar" else "Guardar cambios",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
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
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0B2C4D)),
                            border = BorderStroke(1.dp, Color(0xFFD1D5DB))
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
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                            border = BorderStroke(1.dp, Color(0xFFDC2626))
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
        containerColor = Color(0xFFF9FAFB)
    ) { innerPadding ->
        if (user == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF0B2C4D))
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
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFEF4444))
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
                                    .background(Color(0xFFDC2626), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Error,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "No se guardaron los cambios",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B)
                                )
                                Text(
                                    text = "Revisa los campos marcados e inténtalo de nuevo.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF7F1D1D)
                                )
                            }
                        }
                    }
                }

                // Aviso verde de éxito (Cambios guardados con palomita)
                if (isSavedSuccessfully && !showGlobalError) {
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF16A34A))
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
                                    .background(Color(0xFF16A34A), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Cambios guardados",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                                Text(
                                    text = "La información de ${currentUser.name} se actualizó correctamente.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF14532D)
                                )
                            }
                        }
                    }
                }

                // Tarjeta de usuario principal
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                    .background(Color(0xFF0B2C4D), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser.initials,
                                    color = Color.White,
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
                                color = Color(0xFF1F2937)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = currentUser.email,
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280)
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
                    colors = CardDefaults.cardColors(containerColor = Color.White),
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
                            Text("ROL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF))
                            Spacer(Modifier.height(4.dp))
                            UserRoleTag(currentUser.role)
                        }
                        Column {
                            Text("ESTATUS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF))
                            Spacer(Modifier.height(4.dp))
                            UserStatusBadge(currentUser.status, showBackground = true)
                        }
                        Column {
                            Text("REGISTRO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF))
                            Spacer(Modifier.height(4.dp))
                            Text(currentUser.registrationDate, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1F2937))
                        }
                    }
                }

                // Formulario editable
                Text("Información personal", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))

                // Fila 1: Nombre(s) y Primer apellido
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        val isNomError = nombreError != null
                        val nomContainerColor = if (isNomError) Color(0xFFFEE2E2) else Color.White
                        val nomLabelColor = if (isNomError) Color(0xFFB91C1C) else Color(0xFF374151)

                        Text("Nombre(s)", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = nomLabelColor)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { viewModel.nombre.value = it },
                            isError = isNomError,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isNomError) Color(0xFFEF4444) else Color(0xFF0B2C4D),
                                unfocusedBorderColor = if (isNomError) Color(0xFFEF4444) else Color(0xFFD1D5DB),
                                focusedContainerColor = nomContainerColor,
                                unfocusedContainerColor = nomContainerColor
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (nombreError != null) {
                            Text(nombreError!!, fontSize = 12.sp, color = Color(0xFFB91C1C))
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        val isApError = apellidoError != null
                        val apContainerColor = if (isApError) Color(0xFFFEE2E2) else Color.White
                        val apLabelColor = if (isApError) Color(0xFFB91C1C) else Color(0xFF374151)

                        Text("Primer apellido", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = apLabelColor)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = primerApellido,
                            onValueChange = { viewModel.primerApellido.value = it },
                            isError = isApError,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isApError) Color(0xFFEF4444) else Color(0xFF0B2C4D),
                                unfocusedBorderColor = if (isApError) Color(0xFFEF4444) else Color(0xFFD1D5DB),
                                focusedContainerColor = apContainerColor,
                                unfocusedContainerColor = apContainerColor
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (apellidoError != null) {
                            Text(apellidoError!!, fontSize = 12.sp, color = Color(0xFFB91C1C))
                        }
                    }
                }

                // Segundo apellido (opcional)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Segundo apellido (opcional)", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = segundoApellido,
                        onValueChange = { viewModel.segundoApellido.value = it },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0B2C4D),
                            unfocusedBorderColor = Color(0xFFD1D5DB),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Correo electrónico
                Column(modifier = Modifier.fillMaxWidth()) {
                    val isEmailError = correoError != null
                    val emailContainerColor = if (isEmailError) Color(0xFFFEE2E2) else Color.White
                    val emailLabelColor = if (isEmailError) Color(0xFFB91C1C) else Color(0xFF374151)

                    Text("Correo electrónico", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = emailLabelColor)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = correo,
                        onValueChange = { viewModel.correo.value = it },
                        isError = isEmailError,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isEmailError) Color(0xFFEF4444) else Color(0xFF0B2C4D),
                            unfocusedBorderColor = if (isEmailError) Color(0xFFEF4444) else Color(0xFFD1D5DB),
                            focusedContainerColor = emailContainerColor,
                            unfocusedContainerColor = emailContainerColor
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (correoError != null) {
                        Text(correoError!!, fontSize = 12.sp, color = Color(0xFFB91C1C))
                    }
                }

                // Teléfono
                Column(modifier = Modifier.fillMaxWidth()) {
                    val isPhoneError = telefonoError != null
                    val phoneContainerColor = if (isPhoneError) Color(0xFFFEE2E2) else Color.White
                    val phoneLabelColor = if (isPhoneError) Color(0xFFB91C1C) else Color(0xFF374151)

                    Text("Teléfono", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = phoneLabelColor)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { viewModel.telefono.value = it },
                        isError = isPhoneError,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isPhoneError) Color(0xFFEF4444) else Color(0xFF0B2C4D),
                            unfocusedBorderColor = if (isPhoneError) Color(0xFFEF4444) else Color(0xFFD1D5DB),
                            focusedContainerColor = phoneContainerColor,
                            unfocusedContainerColor = phoneContainerColor
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (telefonoError != null) {
                        Text(telefonoError!!, fontSize = 12.sp, color = Color(0xFFB91C1C))
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}
