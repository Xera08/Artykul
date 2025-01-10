package com.example.artykul

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.artykul.ui.AdminLoginScreen
import com.example.artykul.ui.UserLoginScreen


interface ArtykulDestination {
    val route: String
    val screen: @Composable (navController: NavController) -> Unit
}

object ChooseRole : ArtykulDestination {
    override val route = "chooseRole"
    override val screen: @Composable (navController: NavController) -> Unit = {navController -> ChooseRoleScreen(navController = navController) }
}

object AdminLogin : ArtykulDestination {
    override val route = "adminLogin"
    override val screen: @Composable (navController: NavController) -> Unit = { AdminLoginScreen() }
}

object UserLogin : ArtykulDestination {
    override val route = "userLogin"
    override val screen: @Composable (navController: NavController) -> Unit = { UserLoginScreen() }
}