package com.franlop.android_firebase.model

// Los valores por defecto son necesarios para que Firebase pueda convertir los datos al objeto
data class Contact(
    var key: String? = null,
    val name: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val uid: String = ""
)
