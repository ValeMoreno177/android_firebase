package com.franlop.android_firebase

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.franlop.android_firebase.ui.navigation.Navigation
import com.franlop.android_firebase.ui.theme.AndroidFirebaseTheme
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("FCM", "Permiso de notificaciones: $isGranted")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        askNotificationPermission()
        getFcmToken()

        setContent {
            AndroidFirebaseTheme() {
                Navigation(this)
            }
        }
    }

    // En Android 13+ hay que pedir permiso para mostrar notificaciones
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // El token sirve para mandar una notificación de prueba a este dispositivo desde la consola
    private fun getFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM TOKEN", "No se pudo obtener el token", task.exception)
                return@addOnCompleteListener
            }
            Log.d("FCM TOKEN", task.result)
        }
    }
}
