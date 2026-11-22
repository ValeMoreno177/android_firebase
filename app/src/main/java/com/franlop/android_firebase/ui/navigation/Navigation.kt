package com.franlop.android_firebase.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.franlop.android_firebase.ui.screens.HomeScreen
import com.franlop.android_firebase.ui.screens.Screen
import com.franlop.android_firebase.ui.screens.auth.ForgotPasswordScreen
import com.franlop.android_firebase.ui.screens.auth.LoginScreen
import com.franlop.android_firebase.ui.screens.auth.SignUpScreen
import com.franlop.android_firebase.utils.AnalyticsManager
import com.franlop.android_firebase.utils.AuthManager

@Composable
fun Navigation(context: Context, navController: NavHostController = rememberNavController()) {
    val analytics = remember { AnalyticsManager(context) }
    val authManager = remember { AuthManager(context) }

    // Si ya hay una sesión iniciada, entramos directo al Home
    val startDestination =
        if (authManager.getCurrentUser() != null) Routes.Home.route else Routes.Login.route

    Screen {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(Routes.Login.route) {
                LoginScreen(
                    analytics = analytics,
                    auth = authManager,
                    navigation = navController,
                )
            }
            composable(Routes.Home.route) {
                HomeScreen(
                    analytics = analytics,
                    auth = authManager,
                    navigation = navController
                )
            }
            composable(Routes.SignUp.route) {
                SignUpScreen(
                    analytics = analytics,
                    auth = authManager,
                    navigation = navController
                )
            }
            composable(Routes.ForgotPassword.route) {
                ForgotPasswordScreen(
                    analytics = analytics,
                    auth = authManager,
                    navigation = navController
                )
            }
        }
    }
}
