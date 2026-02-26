package com.yd.weather.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocationData(
    val address: String? = null,
    @SerialName("address_component")
    val addressComponent: AddressComponentData? = null,
)

@Serializable
data class AddressComponentData(
    val nation: String? = null,
    val province: String? = null,
    val city: String? = null,
    val district: String? = null,
    val street: String? = null,
    @SerialName("street_number")
    val streetNumber: String? = null,
)
