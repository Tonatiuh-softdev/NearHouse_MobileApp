package com.equipo4.nearhome.ui.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import com.equipo4.nearhome.util.AppStrings
import com.equipo4.nearhome.R

class CheckoutViewModel(
    val plan: SubscriptionPlan,
    private val repo: SubscriptionRepository
) : ViewModel() {

    val cardNumber = MutableStateFlow(TextFieldValue(""))
    val cardHolder = MutableStateFlow(TextFieldValue(""))
    val expiry = MutableStateFlow(TextFieldValue(""))
    val cvv = MutableStateFlow(TextFieldValue(""))

    val isLoading = MutableStateFlow(false)
    val successMessage = MutableStateFlow<String?>(null)
    val apiError = MutableStateFlow<String?>(null)

    val isComplete: StateFlow<Boolean> = combine(
        cardNumber, cardHolder, expiry, cvv
    ) { num, holder, exp, c ->
        val numDigits = num.text.filter { it.isDigit() }
        val holderText = holder.text
        val expDigits = exp.text.filter { it.isDigit() }
        val cvvText = c.text
        val m = expDigits.take(2).toIntOrNull() ?: 0

        numDigits.length == 16 &&
        holderText.isNotBlank() && holderText.all { it.isLetter() || it.isWhitespace() } &&
        expDigits.length == 4 && m in 1..12 &&
        cvvText.length == 3
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun onCardNumberChanged(input: TextFieldValue) {
        val originalText = input.text
        val oldSelection = input.selection.start
        val digits = originalText.filter { it.isDigit() }.take(16)

        val digitsBeforeCursor = originalText.take(oldSelection.coerceIn(0, originalText.length)).count { it.isDigit() }

        val formatted = StringBuilder()
        var newCursorPos = 0
        var countedDigits = 0

        for (i in digits.indices) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(' ')
                if (countedDigits == digitsBeforeCursor) {
                    newCursorPos = formatted.length - 1
                }
            }
            if (countedDigits == digitsBeforeCursor) {
                newCursorPos = formatted.length
            }
            formatted.append(digits[i])
            countedDigits++
            if (countedDigits == digitsBeforeCursor) {
                newCursorPos = formatted.length
            }
        }
        if (digitsBeforeCursor >= digits.length) {
            newCursorPos = formatted.length
        }

        cardNumber.value = TextFieldValue(
            text = formatted.toString(),
            selection = TextRange(newCursorPos.coerceIn(0, formatted.length))
        )
    }

    fun onCardHolderChanged(input: TextFieldValue) {
        val filteredText = input.text.filter { it.isLetter() || it.isWhitespace() }.take(50)
        val newPos = input.selection.start.coerceAtMost(filteredText.length)
        cardHolder.value = TextFieldValue(text = filteredText, selection = TextRange(newPos))
    }

    fun onExpiryChanged(input: TextFieldValue) {
        val originalText = input.text
        val oldSelection = input.selection.start
        val digits = originalText.filter { it.isDigit() }.take(4)

        val digitsBeforeCursor = originalText.take(oldSelection.coerceIn(0, originalText.length)).count { it.isDigit() }

        val formatted = StringBuilder()
        var newCursorPos = 0
        var countedDigits = 0

        for (i in digits.indices) {
            if (i == 2) {
                formatted.append(" / ")
                if (countedDigits == digitsBeforeCursor) {
                    newCursorPos = formatted.length - 3
                }
            }
            if (countedDigits == digitsBeforeCursor) {
                newCursorPos = formatted.length
            }
            formatted.append(digits[i])
            countedDigits++
            if (countedDigits == digitsBeforeCursor) {
                newCursorPos = formatted.length
            }
        }
        if (digitsBeforeCursor >= digits.length) {
            newCursorPos = formatted.length
        }

        expiry.value = TextFieldValue(
            text = formatted.toString(),
            selection = TextRange(newCursorPos.coerceIn(0, formatted.length))
        )
    }

    fun onCvvChanged(input: TextFieldValue) {
        val filteredText = input.text.filter { it.isDigit() }.take(3)
        val newPos = input.selection.start.coerceAtMost(filteredText.length)
        cvv.value = TextFieldValue(text = filteredText, selection = TextRange(newPos))
    }

    fun submit(onSuccessNavigation: () -> Unit) {
        if (!isComplete.value || isLoading.value) return

        apiError.value = null
        isLoading.value = true

        viewModelScope.launch {
            when (val r = repo.contratar(plan.id)) {
                is Resultado.Ok -> {
                    isLoading.value = false
                    val fecha = r.valor.plan?.let { PlanDates.formatear(it.fechaVencimiento, Locale.getDefault()) } ?: ""
                    successMessage.value = AppStrings.get(R.string.success_plan_active_until, plan.name, fecha)
                    delay(2200)
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
