package com.equipo4.nearhome.ui.subscription

import com.equipo4.nearhome.R
import com.equipo4.nearhome.util.AppStrings
import java.text.SimpleDateFormat
import java.util.Locale
import com.equipo4.nearhome.network.PlanDto

data class SubscriptionPlan(
    val id: Int,
    val name: String,
    val price: Int,
    val durationDays: Int,
    val limit: Int?,
    val visibilityLevel: Int,
    val trialDays: Int,
    val isPopular: Boolean,
    val isFreeTrial: Boolean = false
) {
    val priceText: String
        get() = AppStrings.get(R.string.plan_price_amount, price)

    val description: String
        get() {
            val l = limit
            return when {
                l == null -> AppStrings.get(R.string.plan_limit_unlimited)
                l == 1 -> AppStrings.get(R.string.plan_limit_single)
                else -> AppStrings.get(R.string.plan_limit_multi_format, l)
            }
        }

    val visibility: String
        get() = AppStrings.get(
            when (visibilityLevel) {
                1 -> R.string.plan_vis_1
                2 -> R.string.plan_vis_2
                else -> R.string.plan_vis_3
            }
        )

    fun isFreeTrialFor(pruebaDisponible: Boolean): Boolean = trialDays > 0 && pruebaDisponible
}

fun PlanDto.toModel() = SubscriptionPlan(
    id = id,
    name = nombre,
    price = precio,
    durationDays = duracionDias,
    limit = limiteAnuncios,
    visibilityLevel = nivelVisibilidad,
    trialDays = diasPrueba,
    isPopular = destacado
)

/** Último listado de planes cargado. CheckoutScreen lo usa para encontrar el plan elegido. */
object SubscriptionPlanCache {
    @Volatile
    var plans: List<SubscriptionPlan> = emptyList()

    fun find(id: Int): SubscriptionPlan? = plans.firstOrNull { it.id == id }
}

/** Formatea la fecha de la API ("2026-11-15T...") en el idioma de la app. */
object PlanDates {
    fun formatear(iso: String, locale: Locale): String {
        val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso.take(10)) ?: return iso
        val patron = if (locale.language == "en") "MMMM d, yyyy" else "d 'de' MMMM 'de' yyyy"
        return SimpleDateFormat(patron, locale).format(fecha)
    }
}