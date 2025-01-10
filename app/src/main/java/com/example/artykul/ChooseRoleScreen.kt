package com.example.artykul
import android.R.attr.onClick
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.compose.rememberNavController


@Composable
fun ChooseRoleScreen(navController: NavController) {
    Column() {
        Button(modifier = Modifier.padding(24.dp),
                onClick = {navController.navigate(AdminLogin.route)}) {
            Text("Administrator")
        }
        Button(modifier = Modifier.padding(24.dp),
                onClick = {navController.navigate(UserLogin.route)}) {
            Text("User")
        }
    }
}