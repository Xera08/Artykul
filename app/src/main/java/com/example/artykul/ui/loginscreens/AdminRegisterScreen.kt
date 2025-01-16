package com.example.artykul.ui.loginscreens

import android.util.Log
import androidx.compose.foundation.layout.Column
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
import com.example.artykul.AdminLogin
import com.example.artykul.ChooseRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AdminRegisterScreen(navController: NavController) {
    val adminEmail = remember { mutableStateOf("example@gmail.com") }
    val adminPassword = remember { mutableStateOf("") }
    val adminPasswordConfirm = remember { mutableStateOf("") }
    val showErrorDialog = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ADMIN REGISTER SCREEN",
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

        OutlinedTextField(
            value = adminPasswordConfirm.value,
            onValueChange = { adminPasswordConfirm.value = it },
            label = { Text(text = "confirm password") },
            modifier = Modifier.padding(12.dp)
        )

        Button(
            modifier = Modifier.padding(24.dp),
            onClick = {
                val email = adminEmail.value.trim()

                // Check if email contains exactly one '@' symbol and ends with '@gmail.com'
                if (adminPassword.value == adminPasswordConfirm.value) {
                    if (adminEmail.value.isNotEmpty() && adminPassword.value.isNotEmpty()) {
                        if (email.count { it == '@' } == 1 && email.endsWith("@gmail.com")) {
                            checkAdminEmail(
                                email = adminEmail.value,
                                onExists = {
                                    errorMessage.value = "Email already registered. Please use a different email."
                                    showErrorDialog.value = true
                                },
                                onDoesNotExist = {
                                    registerAdmin(
                                        email = adminEmail.value,
                                        password = adminPassword.value,
                                        onSuccess = {
                                            navController.navigate(AdminLogin.route)
                                        },
                                        onFailure = { error ->
                                            errorMessage.value = error
                                            showErrorDialog.value = true
                                        }
                                    )
                                }
                            )
                        } else {
                            errorMessage.value = "Please enter a valid email ending with '@gmail.com' and containing exactly one '@'."
                            showErrorDialog.value = true
                        }
                    } else {
                        errorMessage.value = "Email and Password cannot be empty."
                        showErrorDialog.value = true
                    }
                } else {
                    errorMessage.value = "Passwords do not match. Please try again."
                    showErrorDialog.value = true
                }
            }
        ) {
            Text("Register")
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

fun checkAdminEmail(
    email: String,
    onExists: () -> Unit,
    onDoesNotExist: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("admins")
        .whereEqualTo("email", email)
        .get()
        .addOnSuccessListener { result ->
            if (!result.isEmpty) {
                onExists() // Email already exists
            } else {
                onDoesNotExist() // Email does not exist
            }
        }
        .addOnFailureListener { e ->
            Log.e("CheckAdminEmail", "Error checking email: ${e.message}")
        }
}

fun registerAdmin(
    email: String,
    password: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    val admin = hashMapOf(
        "email" to email,
        "password" to password // Consider hashing the password
    )

    db.collection("admins")
        .add(admin)
        .addOnSuccessListener {
            Log.d("RegisterAdmin", "Admin registered successfully.")
            onSuccess()
        }
        .addOnFailureListener { e ->
            Log.e("RegisterAdmin", "Error registering admin: ${e.message}")
            onFailure(e.message ?: "An error occurred.")
        }
}
