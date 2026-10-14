package com.equipo4.nearhome.ui.subscription

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val priceText: String,
    val description: String,
    val visibility: String,
    val isPopular: Boolean = false,
    val isFreeTrial: Boolean = false
) {
    companion object {
        val plans = listOf(
            SubscriptionPlan(
                id = "basico",
                name = "Básico",
                priceText = "$99/mes",
                description = "Hasta 1 publicación activa",
                visibility = "Visibilidad estándar",
                isFreeTrial = true
            ),
            SubscriptionPlan(
                id = "profesional",
                name = "Profesional",
                priceText = "$249/mes",
                description = "Hasta 3 publicaciones activas",
                visibility = "Visibilidad aumentada",
                isPopular = true
            ),
            SubscriptionPlan(
                id = "premium",
                name = "Premium",
                priceText = "$449/mes",
                description = "Publicaciones activas ilimitadas",
                visibility = "Visibilidad máxima"
            )
        )

        fun getById(id: String): SubscriptionPlan {
            return plans.firstOrNull { it.id == id } ?: plans.first()
        }
    }
}
