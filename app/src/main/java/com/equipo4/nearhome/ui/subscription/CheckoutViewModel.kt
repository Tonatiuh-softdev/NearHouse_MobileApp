package com.equipo4.nearhome.ui.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

import com.equipo4.nearhome.util.AppStrings
import com.equipo4.nearhome.R

class CheckoutViewModel(
    val plan: SubscriptionPlan,
    private val repo: SubscriptionRepository
) : ViewModel() {

    val cardNumber = MutableStateFlow("")
    val cardHolder = MutableStateFlow("")
    val expiryDate = MutableStateFlow("")
    val cvv = MutableStateFlow("")
    val saveCard = MutableStateFlow(false)

    val cardNumberError = MutableStateFlow<String?>(null)
    val cardHolderError = MutableStateFlow<String?>(null)
    val expiryError = MutableStateFlow<String?>(null)
    val cvvError = MutableStateFlow<String?>(null)

    val showGeneralError = MutableStateFlow(false)
    val isLoading = MutableStateFlow(false)
    val successMessage = MutableStateFlow<String?>(null)
    val apiError = MutableStateFlow<String?>(null)

    fun onCardNumberChanged(input: String) {
        val digitsOnly = input.filter { it.isDigit() }.take(16)
        val formatted = digitsOnly.chunked(4).joinToString(" ")
        cardNumber.value = formatted
        if (cardNumberError.value != null) validateCardNumber()
    }

    fun onCardHolderChanged(input: String) {
        cardHolder.value = input.filter { it.isLetter() || it.isWhitespace() }
        if (cardHolderError.value != null) validateCardHolder()
    }

    fun onExpiryChanged(input: String) {
        val digitsOnly = input.filter { it.isDigit() }.take(4)
        val formatted = when {
            digitsOnly.length > 2 -> "${digitsOnly.substring(0, 2)}/${digitsOnly.substring(2)}"
            else -> digitsOnly
        }
        expiryDate.value = formatted
        if (expiryError.value != null) validateExpiry()
    }

    fun onCvvChanged(input: String) {
        cvv.value = input.filter { it.isDigit() }.take(3)
        if (cvvError.value != null) validateCvv()
    }

    fun validateCardNumber(): Boolean {
        val raw = cardNumber.value.filter { it.isDigit() }
        return if (raw.length != 16 || !isValidLuhn(raw)) {
            cardNumberError.value = AppStrings.get(R.string.error_card_number)
            false
        } else {
            cardNumberError.value = null
            true
        }
    }

    fun validateCardHolder(): Boolean {
        val text = cardHolder.value.trim()
        return if (text.isBlank() || !text.all { it.isLetter() || it.isWhitespace() }) {
            cardHolderError.value = AppStrings.get(R.string.error_card_holder)
            false
        } else {
            cardHolderError.value = null
            true
        }
    }

    fun validateExpiry(): Boolean {
        val parts = expiryDate.value.split("/")
        if (parts.size != 2 || parts[0].length != 2 || parts[1].length != 2) {
            expiryError.value = AppStrings.get(R.string.error_expiry_invalid)
            return false
        }
        val month = parts[0].toIntOrNull() ?: 0
        val year = parts[1].toIntOrNull() ?: 0

        if (month !in 1..12) {
            expiryError.value = AppStrings.get(R.string.error_expiry_invalid)
            return false
        }

        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR) % 100
        val currentMonth = cal.get(Calendar.MONTH) + 1

        if (year < currentYear || (year == currentYear && month < currentMonth)) {
            expiryError.value = AppStrings.get(R.string.error_expiry_expired)
            return false
        }

        expiryError.value = null
        return true
    }

    fun validateCvv(): Boolean {
        return if (cvv.value.length != 3) {
            cvvError.value = AppStrings.get(R.string.error_cvv)
            false
        } else {
            cvvError.value = null
            true
        }
    }

    private fun isValidLuhn(number: String): Boolean {
        var sum = 0
        var alternate = false
        for (i in number.length - 1 downTo 0) {
            var n = number[i].digitToIntOrNull() ?: return false
            if (alternate) {
                n *= 2
                if (n > 9) n -= 9
            }
            sum += n
            alternate = !alternate
        }
        return sum % 10 == 0
    }

    fun submit(onSuccessNavigation: () -> Unit) {
        val v1 = validateCardNumber()
        val v2 = validateCardHolder()
        val v3 = validateExpiry()
        val v4 = validateCvv()

        if (!v1 || !v2 || !v3 || !v4) {
            showGeneralError.value = true
            return
        }

        showGeneralError.value = false
        apiError.value = null
        isLoading.value = true

        viewModelScope.launch {
            // Los datos de la tarjeta NO se mandan a la API: solo el id del plan
            when (val r = repo.contratar(plan.id)) {
                is Resultado.Ok -> {
                    isLoading.value = false
                    successMessage.value = if (plan.isFreeTrial) {
                        AppStrings.get(R.string.success_trial_msg)
                    } else {
                        AppStrings.get(R.string.success_paid_msg, plan.name)
                    }
                    delay(1500)
                    onSuccessNavigation()
                }
                is Resultado.Error -> {
                    isLoading.value = false
                    apiError.value = r.mensaje
                }
            }
        }
    }
}