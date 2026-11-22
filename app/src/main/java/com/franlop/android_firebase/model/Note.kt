package com.franlop.android_firebase.model

// Los valores por defecto son necesarios para que Firestore pueda convertir el documento al objeto
data class Note(
    var id: String? = null,
    val userId: String = "",
    val title: String = "",
    val content: String = ""
)
