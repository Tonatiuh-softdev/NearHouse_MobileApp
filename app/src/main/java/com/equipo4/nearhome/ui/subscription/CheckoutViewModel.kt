package com.equipo4.nearhome.ui.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    val cardNumber = MutableStateFlow("") // max 16 digits
    val cardHolder = MutableStateFlow("") // max 50 chars, letters & spaces
    val expiryMonth = MutableStateFlow("") // max 2 digits, 01-12
    val expiryYear = MutableStateFlow("") // max 2 digits
    val cvv = MutableStateFlow("") // max 3 digits

    val isLoading = MutableStateFlow(false)
    val successMessage = MutableStateFlow<String?>(null)
    val apiError = MutableStateFlow<String?>(null)

    val isComplete: StateFlow<Boolean> = combine(
        cardNumber, cardHolder, expiryMonth, expiryYear, cvv
    ) { num, holder, month, year, c ->
        val m = month.toIntOrNull() ?: 0
        num.length == 16 &&
        holder.isNotBlank() && holder.all { it.isLetter() || it.isWhitespace() } &&
        month.length == 2 && m in 1..12 &&
        year.length == 2 &&
        c.length == 3
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun onCardNumberChanged(input: String) {
        cardNumber.value = input.filter { it.isDigit() }.take(16)
    }

    fun onCardHolderChanged(input: String) {
        cardHolder.value = input.filter { it.isLetter() || it.isWhitespace() }.take(50)
    }

    fun onExpiryMonthChanged(input: String) {
        expiryMonth.value = input.filter { it.isDigit() }.take(2)
    }

    fun onExpiryYearChanged(input: String) {
        expiryYear.value = input.filter { it.isDigit() }.take(2)
    }

    fun onCvvChanged(input: String) {
        cvv.value = input.filter { it.isDigit() }.take(3)
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
