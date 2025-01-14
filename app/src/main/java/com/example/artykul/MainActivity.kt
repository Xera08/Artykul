package com.example.artykul

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.artykul.ui.UsersList
import com.example.artykul.ui.theme.ArtykulTheme
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArtykulTheme {

                ArtykulApp()
            }
        }
    }
}

@Composable
fun ArtykulApp() {
    ArtykulTheme {
        var currentScreen = remember { mutableStateOf(ChooseRole) }
        val navController = rememberNavController()
        //Firebase.initializeApp(context)


        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = ChooseRole.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(route = ChooseRole.route) {
                    ChooseRole.screen(navController)
                }
                composable(route = AdminLogin.route) {
                    AdminLogin.screen(navController)
                }
                composable(route = UserLogin.route) {
                    UserLogin.screen(navController)
                }
                composable(route = AdminRegister.route) {
                    AdminRegister.screen(navController)
                }
                composable(route = UsersList.route) { backStackEntry ->
                    val master = backStackEntry.arguments?.getString("master")
                    if (master != null) {
                        UsersList(master = master)
                    }
                }
            }

        }
    }
}






