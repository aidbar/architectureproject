package com.example.architectureproject.profile

data class UserLifestyle(val disabilities: Set<Disability>,
                         val transportationPreference: TransportationMethod,
                         val diet: Diet,
                         val sustainabilityInfluence: Frequency,
                         val locallySourcedFoodPreference: Frequency,
                         val shoppingPreference: ShoppingMethod) {
    private constructor(builder: Builder) : this(
        builder.disabilities,
        builder.transportationPreference,
        builder.diet,
        builder.sustainabilityInfluence,
        builder.locallySourcedFoodPreference,
        builder.shoppingPreference
    )

    data class Builder(
        var disabilities: Set<Disability> = setOf(),
        var transportationPreference: TransportationMethod = TransportationMethod.PersonalVehicle,
        var diet: Diet = Diet.None,
        var sustainabilityInfluence: Frequency = Frequency.Sometimes,
        var locallySourcedFoodPreference: Frequency = Frequency.Sometimes,
        var shoppingPreference: ShoppingMethod = ShoppingMethod.Both
    ) { fun build() = UserLifestyle(this) }

    enum class ShoppingMethod { Online, InStore, Both }
    enum class Disability { DifficultyWalking }
    enum class Diet { None, Vegetarian, Vegan, Pescatarian }
    enum class Frequency { Always, Sometimes, Rarely, Never }
    enum class TransportationMethod { Walk, Cycle, PublicTransport, PersonalVehicle, Carpool, None }
}