package com.deecode.myapp.domain.calculator

import com.deecode.myapp.domain.model.FareBreakdown
import com.deecode.myapp.domain.model.PricingConfig
import com.deecode.myapp.domain.model.RideTier

interface FareCalculator {
    fun calculateFare(
        tier: RideTier,
        distanceMeters: Int,
        durationSeconds: Long,
        customConfig: PricingConfig? = null
    ): FareBreakdown

    fun calculateAllTiers(
        distanceMeters: Int,
        durationSeconds: Long
    ): Map<RideTier, FareBreakdown>
}
