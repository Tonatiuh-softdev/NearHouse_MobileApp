package com.equipo4.nearhome.ui.profile.seller

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.equipo4.nearhome.R
import com.equipo4.nearhome.ui.common.NearHomeBottomBar
import com.equipo4.nearhome.ui.profile.UserRole

@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel = viewModel(),
    onNavigateToMyListingsWithSnackbar: (message: String) -> Unit,
    onNavigateToAccount: () -> Unit,
    onBottomTabSelected: (Int) -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.role) {
        if (uiState.role == UserRole.COMPRADOR) {
            onNavigateToAccount()
        }
    }

    val updatedSuccessMsg = stringResource(R.string.edit_profile_updated_success)

    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            onNavigateToMyListingsWithSnackbar(updatedSuccessMsg)
        }
    }

    BackHandler {
        viewModel.onAttemptExit { onNavigateToMyListingsWithSnackbar("") }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        viewModel.onCameraImageCaptured(success)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = viewModel.createCameraImageUri(context)
            cameraLauncher.launch(uri)
        } else {
            viewModel.setShowCameraPermissionRationale(true)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        viewModel.onGalleryImagePicked(context, uri)
    }

    Scaffold(
        bottomBar = {
            NearHomeBottomBar(
                selectedTab = 3,
                onTabSelected = { tabIndex ->
                    viewModel.onAttemptExit { onBottomTabSelected(tabIndex) }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                IconButton(
                    onClick = { viewModel.onAttemptExit { onNavigateToMyListingsWithSnackbar("") } }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.edit_profile_back),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clickable { viewModel.openBottomSheet() }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.photoUri != null) {
                            AsyncImage(
                                model = uiState.photoUri,
                                contentDescription = stringResource(R.string.edit_profile_picture_title),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(56.dp)
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.edit_profile_picture_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.clickable { viewModel.openBottomSheet() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                OutlinedTextField(
                    value = uiState.firstName,
                    onValueChange = { viewModel.onFirstNameChange(it) },
                    label = { Text(stringResource(R.string.edit_profile_first_name)) },
                    isError = uiState.firstNameError != null,
                    supportingText = {
                        uiState.firstNameError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.firstLastName,
                    onValueChange = { viewModel.onFirstLastNameChange(it) },
                    label = { Text(stringResource(R.string.edit_profile_first_last_name)) },
                    isError = uiState.firstLastNameError != null,
                    supportingText = {
                        uiState.firstLastNameError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.secondLastName,
                    onValueChange = { viewModel.onSecondLastNameChange(it) },
                    label = { Text(stringResource(R.string.edit_profile_second_last_name)) },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.edit_profile_optional_hint),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    isError = uiState.secondLastNameError != null,
                    supportingText = {
                        uiState.secondLastNameError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = { viewModel.saveProfile() },
                    enabled = uiState.isSaveEnabled,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.edit_profile_save),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (uiState.isBottomSheetOpen) {
        EditProfileBottomSheet(
            photoUri = uiState.photoUri,
            onDismissRequest = { viewModel.closeBottomSheet() },
            onCameraClick = {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onGalleryClick = {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onDeletePhotoClick = { viewModel.onRemovePhoto() }
        )
    }

    if (uiState.showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDiscardDialog() },
            title = { Text(stringResource(R.string.edit_profile_discard_title)) },
            text = { Text(stringResource(R.string.edit_profile_discard_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.dismissDiscardDialog()
                        onNavigateToMyListingsWithSnackbar("")
                    }
                ) {
                    Text(stringResource(R.string.edit_profile_discard_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDiscardDialog() }) {
                    Text(stringResource(R.string.edit_profile_cancel))
                }
            }
        )
    }

    if (uiState.showCameraPermissionRationale) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowCameraPermissionRationale(false) },
            title = { Text(stringResource(R.string.edit_profile_camera_permission_title)) },
            text = { Text(stringResource(R.string.edit_profile_camera_permission_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setShowCameraPermissionRationale(false)
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text(stringResource(R.string.edit_profile_open_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowCameraPermissionRationale(false) }) {
                    Text(stringResource(R.string.edit_profile_cancel))
                }
            }
        )
    }
}