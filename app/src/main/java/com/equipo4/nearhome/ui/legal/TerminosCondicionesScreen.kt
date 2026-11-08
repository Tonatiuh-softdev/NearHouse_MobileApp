package com.equipo4.nearhome.ui.legal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TerminosCondicionesScreen(
    onBackClick: () -> Unit,
    viewModel: LegalDocumentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadTermsAndConditions()
    }

    LegalDocumentScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onSectionSelected = { index -> viewModel.onSectionChanged(index) },
        onToggleDropdown = { expanded -> viewModel.toggleDropdown(expanded) }
    )
}