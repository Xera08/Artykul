package com.example.artykul.ui.loginscreens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.example.artykul.ChooseRole
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun UserLoginScreen(navController: NavController) {
    var userEmail = remember { mutableStateOf("example@gmail.com") }
    var userPassword = remember { mutableStateOf("") }


    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "USER LOGIN SCREEN",
            modifier = Modifier.padding(24.dp),
            fontSize = 24.sp
        )

        OutlinedTextField(
            value = userEmail.value,
            onValueChange = { userEmail.value = it },
            label = { Text(text = "email") },
            modifier = Modifier
                .padding(12.dp)
        )

        OutlinedTextField(
            value = userPassword.value,
            onValueChange = { userPassword.value = it },
            label = { Text(text = "password") },
            modifier = Modifier
                .padding(12.dp)
        )

        Button(modifier = Modifier.padding(24.dp),
                onClick = {
                loginUser(
                        email = userEmail.value,
                        password = userPassword.value,
                        onSuccess = {
                            navController.navigate("ItemsList")
                        },
                        onFailure = { error ->
                            Log.e("UserLogin", error)
                        }
                    )})
            {
            Text("Log in")
        }
    }
}


fun loginUser(
    email: String,
    password: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("users")
        .whereEqualTo("email", email)
        .get()
        .addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents[0]
                val storedPassword = document.getString("password")

                if (storedPassword == password) {
                    onSuccess()
                } else {
                    onFailure("Invalid password.")
                }
            } else {
                onFailure("User email not found.")
            }
        }
        .addOnFailureListener { e ->
            onFailure(e.message ?: "Firestore error")
        }
}
