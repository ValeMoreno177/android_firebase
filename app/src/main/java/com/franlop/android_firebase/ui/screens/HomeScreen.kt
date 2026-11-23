package com.franlop.android_firebase.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.franlop.android_firebase.R
import com.franlop.android_firebase.ui.navigation.Routes
import com.franlop.android_firebase.ui.screens.login.ContactsScreen
import com.franlop.android_firebase.ui.screens.login.NotesScreen
import com.franlop.android_firebase.utils.AnalyticsManager
import com.franlop.android_firebase.utils.AuthManager
import com.franlop.android_firebase.utils.CloudStorageManager
import com.franlop.android_firebase.ui.screens.storage.CloudStorageScreen
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import android.util.Log
import com.franlop.android_firebase.utils.FirestoreManager
import com.franlop.android_firebase.utils.RealtimeManager
import kotlinx.coroutines.launch

// ---------- Remote Config ----------
private const val WELCOME_MESSAGE_KEY = "welcome_message"
private const val IS_BUTTON_VISIBLE_KEY = "is_button_visible"

private var welcomeMessage by mutableStateOf("Bienvenidx")
private var isButtonVisible by mutableStateOf(true)

private fun initRemoteConfig() {
    val remoteConfig = FirebaseRemoteConfig.getInstance()
    val settings = FirebaseRemoteConfigSettings.Builder()
        .setMinimumFetchIntervalInSeconds(3600)
        .build()
    remoteConfig.setConfigSettingsAsync(settings)
    remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

    // Escucha cambios en tiempo real desde la consola
    remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
        override fun onUpdate(configUpdate: ConfigUpdate) {
            Log.d("HomeScreen", "Updated keys: " + configUpdate.updatedKeys)
            if (configUpdate.updatedKeys.contains(WELCOME_MESSAGE_KEY) ||
                configUpdate.updatedKeys.contains(IS_BUTTON_VISIBLE_KEY)
            ) {
                remoteConfig.activate().addOnCompleteListener { displayRemoteValues(remoteConfig) }
            }
        }

        override fun onError(error: FirebaseRemoteConfigException) {
            Log.w("HomeScreen", "Remote Config error", error)
        }
    })

    remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
        if (task.isSuccessful) displayRemoteValues(remoteConfig)
        else Log.w("HomeScreen", "Fetch de Remote Config falló")
    }
}

private fun displayRemoteValues(remoteConfig: FirebaseRemoteConfig) {
    welcomeMessage = remoteConfig.getString(WELCOME_MESSAGE_KEY)
    isButtonVisible = remoteConfig.getBoolean(IS_BUTTON_VISIBLE_KEY)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(analytics: AnalyticsManager, auth: AuthManager, navigation: NavController) {
    analytics.logScreenView(screenName = Routes.Home.route)
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    val user = auth.getCurrentUser()
    val uid = user?.uid ?: ""
    val realtime = remember(uid) { RealtimeManager() }
    val firestore = remember(uid) { FirestoreManager(uid) }
    val storage = remember(uid) { CloudStorageManager(uid) }

    LaunchedEffect(Unit) { initRemoteConfig() }

    var showDialog by remember { mutableStateOf(false) }

    val onLogoutConfirmed: () -> Unit = {
        scope.launch {
            auth.signOut()
            analytics.logButtonClicked("Click: Cerrar sesión")
            navigation.navigate(Routes.Login.route) {
                popUpTo(Routes.Home.route) { inclusive = true }
            }
        }
    }

    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (user?.photoUrl != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(user.photoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Imagen de perfil",
                                placeholder = painterResource(id = R.drawable.profile),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .size(40.dp)
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.profile),
                                contentDescription = "Foto de perfil por defecto",
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (!user?.displayName.isNullOrEmpty()) "Hola ${user?.displayName}" else welcomeMessage,
                                fontSize = 20.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (!user?.email.isNullOrEmpty()) "${user?.email}" else "Anónimo",
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(),
                actions = {
                    // Crashlytics: fuerza un error de prueba (Remote Config decide si se ve)
                    IconButton(
                        onClick = {
                            val crashlytics = FirebaseCrashlytics.getInstance()
                            crashlytics.setCustomKey("pruebaClaveHome", "Valor a enviar")
                            crashlytics.log("Mensaje log desde HomeScreen")
                            crashlytics.setUserId(user?.uid ?: "No Id Found")
                            throw RuntimeException("Error forzado desde HomeScreen")
                        },
                        enabled = isButtonVisible,
                        modifier = Modifier.alpha(if (isButtonVisible) 1f else 0f)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Forzar error")
                    }
                    IconButton(
                        onClick = {
                            showDialog = true
                        }
                    ) {
                        Icon(Icons.Outlined.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        bottomBar = {
            BottomBar(navController = navController)
        }
    ){ contentPadding ->
        Box(modifier = Modifier.padding(contentPadding)) {
            if (showDialog) {
                LogoutDialog(onConfirmLogout = {
                    onLogoutConfirmed()
                    showDialog = false
                }, onDismiss = { showDialog = false })
            }
            BottomNavGraph(navController = navController, realtime = realtime, firestore = firestore, storage = storage, uid = uid)
        }
    }
}

@Composable
fun LogoutDialog(onConfirmLogout: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cerrar sesión") },
        text = { Text("¿Estás seguro que deseas cerrar sesión?") },
        confirmButton = {
            Button(
                onClick = onConfirmLogout
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun BottomBar(navController: NavHostController) {
    val screens = listOf(
        BottomNavScreen.Contact,
        BottomNavScreen.Note,
        BottomNavScreen.Photos
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    NavigationBar {
        screens.forEach { screens ->
            if (currentDestination != null) {
                AddItem(
                    screens = screens,
                    currentDestination = currentDestination,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun RowScope.AddItem(screens: BottomNavScreen, currentDestination: NavDestination, navController: NavHostController) {
    NavigationBarItem(
        label = { Text(text = screens.title) },
        icon = { Icon(imageVector = screens.icon, contentDescription = "Icons") },
        selected = currentDestination.hierarchy?.any {
            it.route == screens.route
        } == true,
        onClick = {
            navController.navigate(screens.route) {
                popUpTo(navController.graph.id)
                launchSingleTop = true
            }
        }
    )
}

@Composable
fun BottomNavGraph(
    navController: NavHostController,
    realtime: RealtimeManager,
    firestore: FirestoreManager,
    storage: CloudStorageManager,
    uid: String
) {
    NavHost(navController = navController, startDestination = BottomNavScreen.Contact.route) {
        composable(route = BottomNavScreen.Contact.route) {
            ContactsScreen(realtime = realtime, uid = uid)
        }
        composable(route = BottomNavScreen.Note.route) {
            NotesScreen(firestore = firestore)
        }
        composable(route = BottomNavScreen.Photos.route) {
            CloudStorageScreen(storage = storage)
        }
    }
}

sealed class BottomNavScreen(val route: String, val title: String, val icon: ImageVector) {
    object Contact : BottomNavScreen(
        route = "contact",
        title = "Contactos",
        icon = Icons.Default.Person
    )
    object Note : BottomNavScreen(
        route = "notes",
        title = "Notas",
        icon = Icons.Default.List
    )
    object Photos : BottomNavScreen(
        route = "photos",
        title = "Fotos",
        icon = Icons.Default.Face
    )
}