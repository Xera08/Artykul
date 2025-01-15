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
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

data class User(val name: String, val email: String, val password: String)

@Composable
fun UsersList() {
    val firestore = FirebaseFirestore.getInstance()
    val usersList = remember { mutableStateOf<List<User>>(emptyList()) }
    val showDialog = remember { mutableStateOf(false) }
    val selectedUser = remember { mutableStateOf<User?>(null) }

    // Fetch users from Firestore
    LaunchedEffect(true) {
        firestore.collection("users")
            .get()
            .addOnSuccessListener { result: QuerySnapshot ->
                val fetchedUsers = result.map { document ->
                    User(
                        name = document.getString("name") ?: "",
                        email = document.getString("email") ?: "",
                        password = document.getString("password") ?: ""
                    )
                }
                usersList.value = fetchedUsers
            }
            .addOnFailureListener { exception ->
                println("Error fetching users: ${exception.message}")
            }
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(4.dp, Color.Black)
                .background(Color.Gray)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "List of all users",
                modifier = Modifier.weight(1f),
                fontSize = 24.sp
            )
            Button(
                modifier = Modifier.padding(10.dp),
                onClick = { showDialog.value = true }
            ) {
                Text("Add user")
            }
        }

        // Dialog for adding a new user
        if (showDialog.value) {
            AddUserDialog(
                onDismiss = { showDialog.value = false },
                onSubmit = { nameValue, emailValue, passwordValue ->
                    val newUser = User(nameValue, emailValue, passwordValue)
                    addUserToFirestore(newUser, firestore)
                    showDialog.value = false
                },
                name = remember { mutableStateOf("") },
                email = remember { mutableStateOf("") },
                password = remember { mutableStateOf("") }
            )
        }

        // Display users list
        usersList.value.forEach { user ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(2.dp, Color.LightGray)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user.name,
                    modifier = Modifier.padding(10.dp),
                    fontSize = 24.sp
                )
                Button(
                    onClick = {
                        selectedUser.value = user
                    }
                ) {
                    Text("Edit")
                }
            }
        }

        // Dialog for editing a user
        selectedUser.value?.let { user ->
            EditUserDialog(
                user = user,
                firestore = firestore,
                onDismiss = { selectedUser.value = null },
                onUpdate = { updatedUser ->
                    updateUserInFirestore(user.email, updatedUser, firestore) { // Use original email
                        selectedUser.value = null
                        usersList.value = usersList.value.map {
                            if (it.email == user.email) updatedUser else it
                        }
                    }
                },
                onDelete = {
                    deleteUserFromFirestore(user, firestore) {
                        selectedUser.value = null
                        usersList.value = usersList.value.filterNot { it.email == user.email }
                    }
                }
            )
        }
    }
}

@Composable
fun EditUserDialog(
    user: User,
    firestore: FirebaseFirestore,
    onDismiss: () -> Unit,
    onUpdate: (User) -> Unit,
    onDelete: () -> Unit
) {
    val name = remember { mutableStateOf(user.name) }
    val email = remember { mutableStateOf(user.email) }
    val password = remember { mutableStateOf(user.password) }
    val showDeleteConfirmation = remember { mutableStateOf(false) }

    if (showDeleteConfirmation.value) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirmation.value = false },
            title = { Text("Delete User") },
            text = { Text("Are you sure you want to delete this user?") },
            confirmButton = {
                Button(onClick = {
                    showDeleteConfirmation.value = false
                    onDelete()
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirmation.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit User") },
        text = {
            Column {
                TextField(
                    value = name.value,
                    onValueChange = { name.value = it },
                    label = { Text("Name") },
                    modifier = Modifier.padding(8.dp)
                )
                TextField(
                    value = email.value,
                    onValueChange = { email.value = it },
                    label = { Text("Email") },
                    modifier = Modifier.padding(8.dp)
                )
                TextField(
                    value = password.value,
                    onValueChange = { password.value = it },
                    label = { Text("Password") },
                    modifier = Modifier.padding(8.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onUpdate(User(name.value, email.value, password.value))
            }) {
                Text("Update")
            }
        },
        dismissButton = {
            Row {
                Button(onClick = { showDeleteConfirmation.value = true }) {
                    Text("Delete")
                }
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

fun updateUserInFirestore(
    originalEmail: String, // Pass the original email before editing
    updatedUser: User,
    firestore: FirebaseFirestore,
    onComplete: () -> Unit
) {
    firestore.collection("users")
        .whereEqualTo("email", originalEmail) // Query using the original email
        .get()
        .addOnSuccessListener { result ->
            val document = result.documents.firstOrNull()
            if (document != null) {
                // Update the document by its ID
                firestore.collection("users").document(document.id)
                    .set(
                        hashMapOf(
                            "name" to updatedUser.name,
                            "email" to updatedUser.email,
                            "password" to updatedUser.password,
                        )
                    )
                    .addOnSuccessListener {
                        println("User updated successfully!")
                        onComplete()
                    }
                    .addOnFailureListener { exception ->
                        println("Error updating user: ${exception.message}")
                    }
            }
        }
        .addOnFailureListener { exception ->
            println("Error fetching user for update: ${exception.message}")
        }
}


fun deleteUserFromFirestore(user: User, firestore: FirebaseFirestore, onComplete: () -> Unit) {
    firestore.collection("users")
        .whereEqualTo("email", user.email)
        .get()
        .addOnSuccessListener { result ->
            val document = result.documents.firstOrNull()
            if (document != null) {
                firestore.collection("users").document(document.id)
                    .delete()
                    .addOnSuccessListener {
                        println("User deleted successfully!")
                        onComplete()
                    }
                    .addOnFailureListener { exception ->
                        println("Error deleting user: ${exception.message}")
                    }
            }
        }
}


// Dialog to input new user information
@Composable
fun AddUserDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit,
    name: MutableState<String>,
    email: MutableState<String>,
    password: MutableState<String>
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New User") },
        text = {
            Column {
                TextField(
                    value = name.value,
                    onValueChange = { name.value = it },
                    label = { Text("Name") },
                    modifier = Modifier.padding(8.dp)
                )
                TextField(
                    value = email.value,
                    onValueChange = { email.value = it },
                    label = { Text("Email") },
                    modifier = Modifier.padding(8.dp)
                )
                TextField(
                    value = password.value,
                    onValueChange = { password.value = it },
                    label = { Text("Password") },
                    modifier = Modifier.padding(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.value.isNotEmpty() && email.value.isNotEmpty() && password.value.isNotEmpty()) {
                        // Submitting the data
                        onSubmit(name.value, email.value, password.value)
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Function to add the new user to Firestore
fun addUserToFirestore(newUser: User, firestore: FirebaseFirestore) {
    val userData = hashMapOf(
        "name" to newUser.name,
        "email" to newUser.email,
        "password" to newUser.password,
    )

    firestore.collection("users")
        .add(userData)
        .addOnSuccessListener {
            println("User added successfully!")
        }
        .addOnFailureListener { exception ->
            println("Error adding user: ${exception.message}")
        }
}

