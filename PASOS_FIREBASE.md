# Qué activar en la consola de Firebase

El código ya está listo, pero sin estos pasos en https://console.firebase.google.com la app no va a funcionar.

## 1. google-services.json
Descárgalo de nuevo después de activar Google (paso 2) y ponlo en `app/google-services.json`.
Sin él no se genera `R.string.default_web_client_id` y el proyecto **no compila**.

## 2. Authentication → Método de acceso
Activa:
- **Correo electrónico/contraseña**
- **Anónimo** (para "Continuar como invitado")
- **Google**: también agrega la huella **SHA-1** de tu PC en Configuración del proyecto → Tus apps → Android.
  Para sacarla, en Android Studio abre la pestaña Gradle y ejecuta `signingReport`, o usa en la terminal:
  `keytool -list -v -keystore %USERPROFILE%\.android\debug.keystore -alias androiddebugkey -storepass android -keypass android`

## 3. Realtime Database (Contactos)
Crea la base de datos y pega estas reglas:
```json
{
  "rules": {
    "contacts": {
      ".indexOn": ["uid"],
      ".read": "auth != null",
      "$key": {
        ".write": "auth != null && (!data.exists() || data.child('uid').val() === auth.uid) && (!newData.exists() || newData.child('uid').val() === auth.uid)"
      }
    }
  }
}
```

## 4. Cloud Firestore (Notas)
Crea la base de datos y pega estas reglas:
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /notes/{noteId} {
      allow read, update, delete: if request.auth != null && resource.data.userId == request.auth.uid;
      allow create: if request.auth != null && request.resource.data.userId == request.auth.uid;
    }
  }
}
```

## 5. Sincroniza y ejecuta
En Android Studio: **Sync Project with Gradle Files** → Run.

## 6. Cloud Storage (Fotos)
Activa Storage y pega estas reglas:
```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /photos/{uid}/{fileName} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
  }
}
```

## 7. Remote Config
Crea dos parámetros y publica los cambios:
- `welcome_message` (String), por ejemplo `Hola desde Remote Config`
- `is_button_visible` (Booleano): `true` muestra el botón ⚠ de Crashlytics en el Home y `false` lo oculta

## 8. Crashlytics
Entra a Crashlytics en la consola. Corre la app, toca "Forzar cierre Crashlytics" (login) o el ⚠ (home),
vuelve a abrir la app y espera unos minutos a que aparezca el error.

## 9. Cloud Messaging (notificaciones)
Acepta el permiso de notificaciones al abrir la app. En el Logcat filtra por `FCM TOKEN` y copia el token.
En la consola entra a Messaging → Nueva campaña → Notificaciones → "Enviar mensaje de prueba" y pega el token.
