package com.equipo4.nearhome.ui.admin.listings

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val MxLocale: Locale = Locale.forLanguageTag("es-MX")

/** 10090.0 -> "10,090" */
fun formatAmount(price: Double): String =
    NumberFormat.getIntegerInstance(Locale.US).format(price)

fun formatDate(timestamp: Long): String =
    SimpleDateFormat("d MMM yyyy", MxLocale).format(Date(timestamp)).replace(".", "")

fun formatDateTime(timestamp: Long): String =
    SimpleDateFormat("d MMM yyyy, HH:mm", MxLocale).format(Date(timestamp)).replace(".", "")
