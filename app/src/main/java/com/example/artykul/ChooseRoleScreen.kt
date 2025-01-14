package com.example.artykul
import android.R.attr.onClick
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController



@Composable
fun ChooseRoleScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), // Added padding around the column for spacing
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Center the content vertically
    ) {
        // Added text above the buttons
        Text(
            text = "Who do you want to log in as?",
            modifier = Modifier.padding(bottom = 24.dp), // Space between text and buttons
            fontSize = 20.sp // Adjust font size if necessary
        )

        // Administrator button
        Button(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(), // Make the button fill the width
            onClick = { navController.navigate(AdminLogin.route) }
        ) {
            Text("Administrator", fontSize = 18.sp) // Larger text for button label
        }

        // User button
        Button(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(), // Make the button fill the width
            onClick = { navController.navigate(UserLogin.route) }
        ) {
            Text("User", fontSize = 18.sp) // Larger text for button label
        }
    }
}
