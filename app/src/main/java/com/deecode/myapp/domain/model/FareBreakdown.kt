package com.deecode.myapp.domain.model

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

data class FareBreakdown(
    val tier: RideTier,
    val baseFare: BigDecimal,
    val distanceCharge: BigDecimal,
    val timeCharge: BigDecimal,
    val totalFare: BigDecimal,
    val distanceKm: Double,
    val durationMinutes: Long
) {
    val formattedTotalFare: String
        get() {
            val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
            format.maximumFractionDigits = 0
            return format.format(totalFare)
        }

    val formattedBaseFare: String
        get() {
            val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
            format.maximumFractionDigits = 0
            return format.format(baseFare)
        }

    val formattedDistanceCharge: String
        get() {
            val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
            format.maximumFractionDigits = 0
            return format.format(distanceCharge)
        }

    val formattedTimeCharge: String
        get() {
            val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
            format.maximumFractionDigits = 0
            return format.format(timeCharge)
        }
}
