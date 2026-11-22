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
