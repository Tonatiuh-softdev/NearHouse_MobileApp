package com.equipo4.nearhome.ui.legal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.equipo4.nearhome.util.AppStrings
import com.equipo4.nearhome.R

class LegalDocumentViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LegalDocumentUiState())
    val uiState: StateFlow<LegalDocumentUiState> = _uiState.asStateFlow()

    fun loadTermsAndConditions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val termsData = LegalDocumentData(
                title = AppStrings.get(R.string.legal_terms_title),
                lastUpdated = AppStrings.get(R.string.legal_terms_updated),
                subtitle = AppStrings.get(R.string.legal_terms_subtitle),
                sections = listOf(
                    LegalSection(
                        id = 1,
                        title = AppStrings.get(R.string.legal_terms_s1_title),
                        content = AppStrings.get(R.string.legal_terms_s1_content)
                    ),
                    LegalSection(
                        id = 2,
                        title = AppStrings.get(R.string.legal_terms_s2_title),
                        content = AppStrings.get(R.string.legal_terms_s2_content)
                    ),
                    LegalSection(
                        id = 3,
                        title = AppStrings.get(R.string.legal_terms_s3_title),
                        content = AppStrings.get(R.string.legal_terms_s3_content)
                    ),
                    LegalSection(
                        id = 4,
                        title = AppStrings.get(R.string.legal_terms_s4_title),
                        content = AppStrings.get(R.string.legal_terms_s4_content)
                    ),
                    LegalSection(
                        id = 5,
                        title = AppStrings.get(R.string.legal_terms_s5_title),
                        content = AppStrings.get(R.string.legal_terms_s5_content)
                    ),
                    LegalSection(
                        id = 6,
                        title = AppStrings.get(R.string.legal_terms_s6_title),
                        content = AppStrings.get(R.string.legal_terms_s6_content)
                    ),
                    LegalSection(
                        id = 7,
                        title = AppStrings.get(R.string.legal_terms_s7_title),
                        content = AppStrings.get(R.string.legal_terms_s7_content)
                    )
                )
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    documentData = termsData
                )
            }
        }
    }

    fun loadPrivacyPolicy() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val privacyData = LegalDocumentData(
                title = AppStrings.get(R.string.legal_privacy_title),
                lastUpdated = AppStrings.get(R.string.legal_privacy_updated),
                subtitle = AppStrings.get(R.string.legal_privacy_subtitle),
                sections = listOf(
                    LegalSection(
                        id = 1,
                        title = AppStrings.get(R.string.legal_privacy_s1_title),
                        content = AppStrings.get(R.string.legal_privacy_s1_content)
                    ),
                    LegalSection(
                        id = 2,
                        title = AppStrings.get(R.string.legal_privacy_s2_title),
                        content = AppStrings.get(R.string.legal_privacy_s2_content)
                    ),
                    LegalSection(
                        id = 3,
                        title = AppStrings.get(R.string.legal_privacy_s3_title),
                        content = AppStrings.get(R.string.legal_privacy_s3_content)
                    ),
                    LegalSection(
                        id = 4,
                        title = AppStrings.get(R.string.legal_privacy_s4_title),
                        content = AppStrings.get(R.string.legal_privacy_s4_content)
                    ),
                    LegalSection(
                        id = 5,
                        title = AppStrings.get(R.string.legal_privacy_s5_title),
                        content = AppStrings.get(R.string.legal_privacy_s5_content)
                    ),
                    LegalSection(
                        id = 6,
                        title = AppStrings.get(R.string.legal_privacy_s6_title),
                        content = AppStrings.get(R.string.legal_privacy_s6_content)
                    ),
                    LegalSection(
                        id = 7,
                        title = AppStrings.get(R.string.legal_privacy_s7_title),
                        content = AppStrings.get(R.string.legal_privacy_s7_content)
                    )
                )
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    documentData = privacyData
                )
            }
        }
    }

    fun onSectionChanged(index: Int) {
        _uiState.update { it.copy(activeSectionIndex = index) }
    }

    fun toggleDropdown(expanded: Boolean? = null) {
        _uiState.update {
            it.copy(isDropdownExpanded = expanded ?: !it.isDropdownExpanded)
        }
    }
}