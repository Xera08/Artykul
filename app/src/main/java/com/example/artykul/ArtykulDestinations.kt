package com.example.artykul

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.artykul.ui.UsersList
import com.example.artykul.ui.loginscreens.AdminRegisterScreen
import com.example.artykul.ui.loginscreens.AdminLoginScreen
import com.example.artykul.ui.loginscreens.UserLoginScreen


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
    override val screen: @Composable (navController: NavController) -> Unit = {navController -> AdminLoginScreen(navController = navController) }
}

object AdminRegister : ArtykulDestination {
    override val route = "adminRegister"
    override val screen: @Composable (navController: NavController) -> Unit = {navController -> AdminRegisterScreen(navController = navController) }
}

object UserLogin : ArtykulDestination {
    override val route = "userLogin"
    override val screen: @Composable (navController: NavController) -> Unit = {navController-> UserLoginScreen(navController = navController) }
}

object UsersList : ArtykulDestination {
    override val route = "usersList/{master}" // 'master' is the parameter
    override val screen: @Composable (navController: NavController) -> Unit = { navController ->
        val master = navController.previousBackStackEntry?.arguments?.getString("master")
        if (master != null) {
            UsersList(master = master)
        }
    }
}
