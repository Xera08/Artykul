package com.example.artykul.ui.loginscreens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.artykul.AdminRegister
import com.example.artykul.UsersList
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AdminLoginScreen(navController: NavController) {
    val adminEmail = remember { mutableStateOf("example@gmail.com") }
    val adminPassword = remember { mutableStateOf("") }
    val showErrorDialog = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "ADMIN LOGIN SCREEN",
            modifier = Modifier.padding(24.dp),
            fontSize = 24.sp
        )

        OutlinedTextField(
            value = adminEmail.value,
            onValueChange = { adminEmail.value = it },
            label = { Text(text = "email") },
            modifier = Modifier.padding(12.dp)
        )

        OutlinedTextField(
            value = adminPassword.value,
            onValueChange = { adminPassword.value = it },
            label = { Text(text = "password") },
            modifier = Modifier.padding(12.dp)
        )

        Button(
            modifier = Modifier.padding(24.dp),
            onClick = {
                if (isEmailValid(adminEmail.value)) {
                    loginAdmin(navController, adminEmail.value, adminPassword.value)
                } else {
                    errorMessage.value = "Please enter a valid email ending with '@gmail.com' and containing exactly one '@'."
                    showErrorDialog.value = true
                }
            }
        ) {
            Text("Log in")
        }

        // Registration proposal row
        Row(modifier = Modifier.align(Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Haven't registered yet?")
            Button(
                modifier = Modifier.padding(24.dp),
                onClick = { navController.navigate(AdminRegister.route) }
            ) {
                Text("Register form")
            }
        }

        // Error Dialog
        if (showErrorDialog.value) {
            AlertDialog(
                onDismissRequest = { showErrorDialog.value = false },
                confirmButton = {
                    Button(onClick = { showErrorDialog.value = false }) {
                        Text("OK")
                    }
                },
                title = { Text("Error") },
                text = { Text(errorMessage.value) }
            )
        }
    }
}

// Email validation function
fun isEmailValid(email: String): Boolean {
    return email.count { it == '@' } == 1 && email.endsWith("@gmail.com")
}

fun loginAdmin(navController: NavController, email: String, password: String) {
    // Check if email and password match (assuming you have a method to check in Firestore)
    val db = FirebaseFirestore.getInstance()
    val adminsCollection = db.collection("admins")

    // Query the Firestore admins collection for the email and password
    adminsCollection
        .whereEqualTo("email", email)
        .whereEqualTo("password", password)
        .get()
        .addOnSuccessListener { documents ->
            if (documents.isEmpty) {
                // Handle invalid credentials (you can show an error message here)
                Log.d("Login", "Invalid credentials")
                // Optionally, show a UI message like Toast or Snackbar
            } else {
                // Admin successfully logged in, navigate to UsersList screen
                Log.d("Login", "Admin logged in successfully")
                val master = email  // The master field is the admin's email
                navController.navigate("usersList/${master}")  // Pass the email as a parameter to UsersList
            }
        }
        .addOnFailureListener { exception ->
            // Handle any errors
            Log.d("Login", "Error getting documents: $exception")
        }
}
