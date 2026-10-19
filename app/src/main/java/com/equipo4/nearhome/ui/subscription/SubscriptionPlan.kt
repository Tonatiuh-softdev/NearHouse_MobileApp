package com.equipo4.nearhome.ui.subscription

import com.equipo4.nearhome.util.AppStrings
import com.equipo4.nearhome.R

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
        val plans: List<SubscriptionPlan>
            get() = listOf(
                SubscriptionPlan(
                    id = "basico",
                    name = AppStrings.get(R.string.plan_basic_name),
                    priceText = AppStrings.get(R.string.plan_basic_price),
                    description = AppStrings.get(R.string.plan_basic_desc),
                    visibility = AppStrings.get(R.string.plan_basic_vis),
                    isFreeTrial = true
                ),
                SubscriptionPlan(
                    id = "profesional",
                    name = AppStrings.get(R.string.plan_professional_name),
                    priceText = AppStrings.get(R.string.plan_professional_price),
                    description = AppStrings.get(R.string.plan_professional_desc),
                    visibility = AppStrings.get(R.string.plan_professional_vis),
                    isPopular = true
                ),
                SubscriptionPlan(
                    id = "premium",
                    name = AppStrings.get(R.string.plan_premium_name),
                    priceText = AppStrings.get(R.string.plan_premium_price),
                    description = AppStrings.get(R.string.plan_premium_desc),
                    visibility = AppStrings.get(R.string.plan_premium_vis)
                )
            )

        fun getById(id: String): SubscriptionPlan {
            return plans.firstOrNull { it.id == id } ?: plans.first()
        }
    }
}