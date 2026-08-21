package com.deecode.myapp.domain.model

enum class RideTier(
    val id: String,
    val displayName: String,
    val description: String,
    val icon: String,
    val capacity: Int
) {
    MINI(
        id = "mini",
        displayName = "JJN Mini",
        description = "Affordable, everyday rides",
        icon = "🚕",
        capacity = 4
    ),
    PRIME(
        id = "prime",
        displayName = "JJN Prime",
        description = "Comfort sedans & top drivers",
        icon = "🚘",
        capacity = 4
    ),
    SUV(
        id = "suv",
        displayName = "JJN SUV",
        description = "Spacious 6-seater SUVs",
        icon = "🚙",
        capacity = 6
    )
}
