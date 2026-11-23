plugins {
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.franlop.android_firebase"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "io.devexpert.android_firebase"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(platform("com.google.firebase:firebase-bom:35.1.0"))
    implementation("com.google.firebase:firebase-analytics")
    // Authentication (correo, anónimo y Google)
    implementation("com.google.firebase:firebase-auth")
    // Realtime Database (Contactos)
    implementation("com.google.firebase:firebase-database")
    // Cloud Firestore (Notas)
    implementation("com.google.firebase:firebase-firestore")
    // Cloud Storage (Fotos)
    implementation("com.google.firebase:firebase-storage")
    // Crashlytics, Remote Config y Cloud Messaging (notificaciones)
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-config")
    implementation("com.google.firebase:firebase-messaging")
    // Para usar .await() en las tareas de Firebase con corrutinas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")

    // Inicio de sesión con Google (Credential Manager)
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    implementation("androidx.navigation:navigation-compose:2.8.0")

    implementation("io.coil-kt:coil-compose:2.4.0")

    implementation("androidx.compose.material:material-icons-core")

    // Iconos extendidos (recomendado para tener acceso a todo el catálogo)
    implementation("androidx.compose.material:material-icons-extended")
}