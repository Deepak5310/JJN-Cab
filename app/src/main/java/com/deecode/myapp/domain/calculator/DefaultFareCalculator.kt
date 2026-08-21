package com.deecode.myapp.domain.calculator

import com.deecode.myapp.domain.model.FareBreakdown
import com.deecode.myapp.domain.model.PricingConfig
import com.deecode.myapp.domain.model.RideTier
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlin.math.ceil

class DefaultFareCalculator @Inject constructor() : FareCalculator {

    private val defaultConfigs = mapOf(
        RideTier.MINI to PricingConfig(
            tier = RideTier.MINI,
            baseFare = BigDecimal("40.00"),
            perKmRate = BigDecimal("11.50"),
            perMinuteRate = BigDecimal("1.50"),
            minimumFare = BigDecimal("60.00")
        ),
        RideTier.PRIME to PricingConfig(
            tier = RideTier.PRIME,
            baseFare = BigDecimal("60.00"),
            perKmRate = BigDecimal("15.00"),
            perMinuteRate = BigDecimal("2.00"),
            minimumFare = BigDecimal("85.00")
        ),
        RideTier.SUV to PricingConfig(
            tier = RideTier.SUV,
            baseFare = BigDecimal("90.00"),
            perKmRate = BigDecimal("20.00"),
            perMinuteRate = BigDecimal("2.50"),
            minimumFare = BigDecimal("130.00")
        )
    )

    override fun calculateFare(
        tier: RideTier,
        distanceMeters: Int,
        durationSeconds: Long,
        customConfig: PricingConfig?
    ): FareBreakdown {
        val config = customConfig ?: defaultConfigs[tier] ?: defaultConfigs.getValue(RideTier.MINI)

        val distanceKm = (distanceMeters / 1000.0).coerceAtLeast(0.0)
        val durationMinutes = ceil(durationSeconds / 60.0).toLong().coerceAtLeast(0L)

        val distanceCharge = BigDecimal.valueOf(distanceKm)
            .multiply(config.perKmRate)
            .setScale(2, RoundingMode.HALF_UP)

        val timeCharge = BigDecimal.valueOf(durationMinutes)
            .multiply(config.perMinuteRate)
            .setScale(2, RoundingMode.HALF_UP)

        val subtotal = config.baseFare
            .add(distanceCharge)
            .add(timeCharge)

        val totalFare = subtotal
            .max(config.minimumFare)
            .setScale(0, RoundingMode.HALF_UP)

        return FareBreakdown(
            tier = tier,
            baseFare = config.baseFare.setScale(0, RoundingMode.HALF_UP),
            distanceCharge = distanceCharge.setScale(0, RoundingMode.HALF_UP),
            timeCharge = timeCharge.setScale(0, RoundingMode.HALF_UP),
            totalFare = totalFare,
            distanceKm = distanceKm,
            durationMinutes = durationMinutes
        )
    }

    override fun calculateAllTiers(
        distanceMeters: Int,
        durationSeconds: Long
    ): Map<RideTier, FareBreakdown> {
        return RideTier.entries.associateWith { tier ->
            calculateFare(tier, distanceMeters, durationSeconds)
        }
    }
}
