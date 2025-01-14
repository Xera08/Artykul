package com.example.artykul.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

data class User(val name: String, val email: String, val password: String)

@Composable
fun UsersList(master: String) {
    val firestore = FirebaseFirestore.getInstance()
    val usersList = remember { mutableStateOf<List<User>>(emptyList()) }

    // Query Firestore to fetch users whose 'master' field matches the admin's email
    LaunchedEffect(master) {
        firestore.collection("users")
            .whereEqualTo("master", master)
            .get()
            .addOnSuccessListener { result: QuerySnapshot ->
                val fetchedUsers = result.map { document ->
                    // Extract user data from Firestore document
                    User(
                        name = document.getString("name") ?: "",
                        email = document.getString("email") ?: "",
                        password = document.getString("password") ?: ""
                    )
                }
                // Update the state with the fetched users
                usersList.value = fetchedUsers
            }
            .addOnFailureListener { exception ->
                // Handle error fetching data
                println("Error fetching users: ${exception.message}")
            }
    }

    Column {
        Row(modifier = Modifier
            .fillMaxWidth()
            .border(4.dp, Color.Black)
            .background(Color.Gray),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly

        ) {
            Text(text = "List of all users",
                modifier = Modifier,
                fontSize = 24.sp
                )
            Button(modifier = Modifier.padding(10.dp),
                onClick = {})
            {
                Text("Add user")
            }
        }
        usersList.value.forEach { user ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(2.dp, Color.LightGray)
                    .padding(8.dp)

            ) {
                Text(text = user.name,
                    modifier = Modifier.padding(10.dp),
                    fontSize = 24.sp,
                )
            }
        }
    }
}

