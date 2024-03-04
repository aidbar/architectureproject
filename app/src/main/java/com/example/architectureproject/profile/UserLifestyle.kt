package com.example.architectureproject.profile

import com.example.architectureproject.tracking.Purchase
import com.example.architectureproject.tracking.Transportation

data class UserLifestyle(val disabilities: List<Disability>,
                         val recycledItems: List<RecyclableItem>,
                         val preferredTransportationMode: Transportation.Mode,
                         val allergies: List<Allergy>,
                         val purchasingPreference: Purchase.Source
    ) {
    enum class Disability { /* TODO */ }
    enum class RecyclableItem { Glass, Cardboard, Paper, Plastic }
    enum class Allergy { Fish, Gluten, Pollen, Nut, Dairy, Soy, Egg }
}