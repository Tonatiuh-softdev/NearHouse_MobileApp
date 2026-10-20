package com.equipo4.nearhome.ui.auth.register



import com.equipo4.nearhome.util.AppStrings
import com.equipo4.nearhome.R
enum class SignUpStep {
    FORM,
    OTP_VERIFICATION,
    SUCCESS
}

data class SignUpUiState(
    val step: SignUpStep = SignUpStep.FORM,
    val email: String = "",
    val emailErrorMessage: String? = null,
    val password: String = "",
    val confirmPassword: String = "",
    val isTermsAccepted: Boolean = false,
    val showTermsError: Boolean = false,
    val otpCode: List<String> = List(6) { "" },
    val isOtpError: Boolean = false,
    val otpErrorMessage: String? = null,
    val resendCountdown: Int = 30,
    val canResendCode: Boolean = false,
    val isLoading: Boolean = false,
    val googleSuccessMessage: String? = null
) {
    // 🔹 Limpia espacios accidentales al inicio y al final
    private val cleanEmail: String get() = email.trim()

    private val allowedDomains = listOf("gmail", "hotmail", "outlook", "ucol")
    private val allowedExtensions = listOf("com", "mx", "edu.mx", "org", "net")

    private fun isDomainBaseAllowed(domainPart: String): Boolean {
        return allowedDomains.any { domain ->
            domainPart == domain || domainPart.startsWith("$domain.")
        }
    }

    private fun hasValidDomainExtension(domainPart: String): Boolean {
        return allowedDomains.any { domain ->
            allowedExtensions.any { ext ->
                domainPart == "$domain.$ext"
            }
        }
    }

    // Validaciones de Correo sobre cleanEmail
    val isEmailValid: Boolean
        get() {
            if (cleanEmail.isBlank() || !cleanEmail.contains("@")) return false
            val parts = cleanEmail.split("@", limit = 2)
            if (parts.size != 2) return false

            val localPart = parts[0]
            val domainPart = parts[1].lowercase()

            if (localPart.isEmpty() || domainPart.isEmpty()) return false
            if (!hasValidDomainExtension(domainPart)) return false

            return android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()
        }

    val showEmailError: Boolean
        get() = email.isNotEmpty() && (!isEmailValid || emailErrorMessage != null)

    val displayEmailError: String?
        get() {
            if (emailErrorMessage != null) return emailErrorMessage
            if (cleanEmail.isEmpty()) return null
            if (!cleanEmail.contains("@")) return AppStrings.get(R.string.signup_email_err_at)

            val parts = cleanEmail.split("@", limit = 2)
            val localPart = parts[0]
            val domainPart = parts.getOrNull(1)?.lowercase().orEmpty()

            if (localPart.isEmpty()) return AppStrings.get(R.string.signup_email_err_local)
            if (domainPart.isEmpty()) return AppStrings.get(R.string.signup_email_err_domain)
            if (!isDomainBaseAllowed(domainPart)) return AppStrings.get(R.string.signup_email_err_allowed)
            if (!hasValidDomainExtension(domainPart)) return AppStrings.get(R.string.signup_email_err_ext)
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) return AppStrings.get(R.string.signup_email_err_format)

            return null
        }

    // Validaciones de Contraseña
    private val hasMinLength: Boolean get() = password.length >= 8
    private val hasLowercase: Boolean get() = password.any { it.isLowerCase() }
    private val hasUppercase: Boolean get() = password.any { it.isUpperCase() }
    private val hasDigit: Boolean get() = password.any { it.isDigit() }
    private val hasNoRepeatedDigits: Boolean get() = !Regex("([0-9])\\1").containsMatchIn(password)

    val isPasswordValid: Boolean
        get() = hasMinLength && hasLowercase && hasUppercase && hasDigit && hasNoRepeatedDigits

    val showPasswordError: Boolean
        get() = password.isNotEmpty() && !isPasswordValid

    val passwordErrorMessage: String?
        get() {
            if (password.isEmpty()) return null
            if (!hasMinLength) return AppStrings.get(R.string.signup_pw_err_length)
            if (!hasLowercase) return AppStrings.get(R.string.signup_pw_err_lower)
            if (!hasUppercase) return AppStrings.get(R.string.signup_pw_err_upper)
            if (!hasDigit) return AppStrings.get(R.string.signup_pw_err_digit)
            if (!hasNoRepeatedDigits) return AppStrings.get(R.string.signup_pw_err_repeated)
            return null
        }

    val doPasswordsMatch: Boolean get() = password.isNotEmpty() && password == confirmPassword
    val areFieldsValid: Boolean get() = isEmailValid && isPasswordValid && doPasswordsMatch
    val isFormValid: Boolean get() = areFieldsValid && isTermsAccepted
    val isOtpComplete: Boolean get() = otpCode.all { it.isNotEmpty() }

    val showPasswordMismatchError: Boolean
        get() = confirmPassword.isNotEmpty() && !doPasswordsMatch
}