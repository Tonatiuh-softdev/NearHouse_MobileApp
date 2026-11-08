package com.equipo4.nearhome.ui.auth.completeprofile

import com.equipo4.nearhome.domain.model.Country

data class CompleteProfileUiState(
    val firstName: String = "",
    val firstLastName: String = "",
    val secondLastName: String = "",
    val birthDate: String = "", // Formato DD/MM/AAAA
    val phoneNumber: String = "",
    val selectedCountry: Country = Country("México", "+52", "🇲🇽", "MX"),

    // Rastreo de interacción de foco (onFocusChanged)
    val firstNameTouched: Boolean = false,
    val firstLastNameTouched: Boolean = false,
    val birthDateTouched: Boolean = false,
    val phoneTouched: Boolean = false,

    // Banderas de error visual y mensajes
    val isFirstNameError: Boolean = false,
    val isFirstLastNameError: Boolean = false,
    val isBirthDateError: Boolean = false,
    val birthDateErrorMessage: String? = null,
    val isPhoneError: Boolean = false,
    val phoneErrorMessage: String? = null,

    // Estado general del formulario
    val showGeneralError: Boolean = false,
    val isFormValid: Boolean = false,
    val isSubmittedAttempted: Boolean = false,
    val isLoading: Boolean = false, // <--- Banderita agregada

    // Estado del BottomSheet de Selección de País
    val isBottomSheetOpen: Boolean = false,
    val countrySearchQuery: String = "",
    val filteredCountries: List<Country> = emptyList()
)