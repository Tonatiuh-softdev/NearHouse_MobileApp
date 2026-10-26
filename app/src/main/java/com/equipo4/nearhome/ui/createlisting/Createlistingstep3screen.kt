package com.equipo4.nearhome.ui.createlisting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.PropertyType
import com.equipo4.nearhome.ui.common.NearHomeBottomBar

@Composable
fun CreateListingStep3Screen(
    viewModel: CreateListingViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onBottomTabSelected: (Int) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme
    val required = stringResource(R.string.create_listing_error_required)
    val descriptionTooShort = stringResource(R.string.create_listing_error_description_min)
    val rulesNoPets = stringResource(R.string.create_listing_rules_pets_no)
    val rulesPets = stringResource(R.string.create_listing_rules_pets_yes)
    var pendingTab by remember { mutableStateOf<Int?>(null) }

    val onTab: (Int) -> Unit = { index ->
        if (state.hasData()) pendingTab = index else onBottomTabSelected(index)
    }

    // Contacto del propietario: sale de la sesión o de la API, nunca se captura aquí
    LaunchedEffect(Unit) {
        if (viewModel.state.value.contact == null) {
            viewModel.setContact(ListingContactLoader.load(context))
        }
    }

    // Reglas sugeridas al rentar (solo si el usuario no las ha editado)
    LaunchedEffect(Unit) {
        val s = viewModel.state.value
        if (s.isRent && s.rules.isBlank() && "rules" !in s.touched) {
            viewModel.update { it.copy(rules = if (it.petsAllowed) rulesPets else rulesNoPets) }
        }
    }

    Scaffold(
        containerColor = cs.surface,
        topBar = { StepHeader(stringResource(R.string.create_listing_step3_title), step = 3, onBack = onBack) },
        bottomBar = {
            Column(Modifier.background(cs.surface)) {
                Button(
                    onClick = onContinue,
                    enabled = state.isStep3Valid,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(48.dp)
                ) { Text(stringResource(R.string.create_listing_continue), fontWeight = FontWeight.SemiBold) }
                NearHomeBottomBar(selectedTab = 3, onTabSelected = onTab)
            }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.create_listing_step3_heading),
                    style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.create_listing_step3_subtitle),
                    style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant
                )
            }

            // Título
            LabeledField(
                stringResource(R.string.create_listing_ad_title),
                if (state.isInvalid("title")) required else null
            ) {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = { v -> viewModel.update("title") { it.copy(title = v.take(MAX_TITLE)) } },
                    singleLine = true,
                    isError = state.isInvalid("title"),
                    supportingText = { Text("${state.title.length}/$MAX_TITLE") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Descripción (mínimo 30 caracteres). En casa y departamento aquí va la superficie (m²)
            LabeledField(
                stringResource(R.string.create_listing_ad_description),
                if (state.isInvalid("description")) {
                    if (state.description.isBlank()) required else descriptionTooShort
                } else null
            ) {
                OutlinedTextField(
                    value = state.description,
                    onValueChange = { v ->
                        viewModel.update("description") { it.copy(description = v.take(MAX_DESCRIPTION)) }
                    },
                    minLines = 4, maxLines = 8,
                    isError = state.isInvalid("description"),
                    supportingText = {
                        Column {
                            if (state.propertyType != PropertyType.TERRENO) {
                                Text(stringResource(R.string.create_listing_description_area_hint))
                            }
                            Text("${state.description.length}/$MAX_DESCRIPTION")
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Reglas (opcional)
            LabeledField(stringResource(R.string.create_listing_ad_rules), null) {
                OutlinedTextField(
                    value = state.rules,
                    onValueChange = { v -> viewModel.update("rules") { it.copy(rules = v.take(MAX_RULES)) } },
                    minLines = 2, maxLines = 5,
                    placeholder = { Text(stringResource(R.string.create_listing_ad_rules_hint)) },
                    supportingText = { Text("${state.rules.length}/$MAX_RULES") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            ContactCard(state.contact)
        }
    }

    if (pendingTab != null) {
        DiscardDialog(
            onConfirm = {
                val tab = pendingTab
                pendingTab = null
                viewModel.reset()
                if (tab != null) onBottomTabSelected(tab)
            },
            onDismiss = { pendingTab = null }
        )
    }
}

/** Tarjeta "Contacto del propietario": solo lectura. */
@Composable
private fun ContactCard(contact: ContactoUsuario?) {
    val cs = MaterialTheme.colorScheme
    val missing = stringResource(R.string.create_listing_contact_missing)
    Column(
        Modifier.fillMaxWidth()
            .border(1.dp, cs.outlineVariant, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.create_listing_contact_title),
                style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(R.string.create_listing_contact_readonly),
                style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant,
                modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(cs.surfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
        if (contact == null) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        } else {
            if (contact.nombre.isNotBlank()) {
                Text(contact.nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
            Text(
                stringResource(R.string.create_listing_contact_whatsapp, contact.whatsapp ?: missing),
                style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
            )
            Text(
                stringResource(R.string.create_listing_contact_email),
                style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant
            )
            Text(contact.correo ?: missing, style = MaterialTheme.typography.bodyMedium)
        }
    }
}