package com.izhenius.aigent.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.izhenius.aigent.presentation.view.ChatScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = AppNavDestinations.CHAT.name) {
        composable(AppNavDestinations.CHAT.name) {
            ChatScreen()
        }
    }
}
