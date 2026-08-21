package com.deecode.myapp.domain.model

import java.math.BigDecimal

data class PricingConfig(
    val tier: RideTier,
    val baseFare: BigDecimal,
    val perKmRate: BigDecimal,
    val perMinuteRate: BigDecimal,
    val minimumFare: BigDecimal
)
