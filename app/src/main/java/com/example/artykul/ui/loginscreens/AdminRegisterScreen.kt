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
    var adminEmail = remember { mutableStateOf("example@gmail.com") }
    var adminPassword = remember { mutableStateOf("") }
    var adminPasswordConfirm = remember { mutableStateOf("") }
    var showErrorDialog = remember { mutableStateOf(false) }


    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "ADMIN REGISTER SCREEN",
            modifier = Modifier.padding(24.dp),
            fontSize = 24.sp
        )


        OutlinedTextField(
            value = adminEmail.value,
            onValueChange = { adminEmail.value = it },
            label = { Text(text = "email") },
            modifier = Modifier
                .padding(12.dp)
        )

        OutlinedTextField(
            value = adminPassword.value,
            onValueChange = { adminPassword.value = it },
            label = { Text(text = "password") },
            modifier = Modifier
                .padding(12.dp)
        )

        OutlinedTextField(
            value = adminPasswordConfirm.value,
            onValueChange = { adminPasswordConfirm.value = it },
            label = { Text(text = "confirm password") },
            modifier = Modifier
                .padding(12.dp)
        )

        Button(modifier = Modifier.padding(24.dp),
            onClick = {
                if (adminPassword.value == adminPasswordConfirm.value) {
                    // Add admin to Firestore and navigate back
                    navController.navigate(AdminLogin.route)
                } else {
                    // Show error dialog
                    showErrorDialog.value = true
                }
            }) {
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
                text = { Text("Passwords do not match. Please try again.") }
            )
        }

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
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { e -> onFailure(e.message ?: "Firestore error") }
}
