package com.example.artykul.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

data class User(val name: String, val email: String, val password: String)

@Composable
fun UsersList() {
    val firestore = FirebaseFirestore.getInstance()
    val usersList = remember { mutableStateOf<List<User>>(emptyList()) }
    val showDialog = remember { mutableStateOf(false) }
    val selectedUser = remember { mutableStateOf<User?>(null) }

    // Function to fetch users from Firestore
    fun refreshUsers() {
        firestore.collection("users")
            .get()
            .addOnSuccessListener { result ->
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

    // Fetch users initially
    LaunchedEffect(Unit) {
        refreshUsers()
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
                onSubmit = { newUser ->
                    firestore.collection("users")
                        .add(
                            hashMapOf(
                                "name" to newUser.name,
                                "email" to newUser.email,
                                "password" to newUser.password
                            )
                        )
                        .addOnSuccessListener {
                            println("User added successfully!")
                            refreshUsers() // Refresh the users' list after adding
                            showDialog.value = false
                        }
                        .addOnFailureListener { exception ->
                            println("Error adding user: ${exception.message}")
                        }
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
                    .border(1.dp, Color.Gray)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user.name,
                    modifier = Modifier.padding(10.dp),
                    fontSize = 18.sp
                )
                Button(
                    onClick = { selectedUser.value = user }
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
                    updateUserInFirestore(user.email, updatedUser, firestore) {
                        selectedUser.value = null
                        refreshUsers() // Refresh the users' list after editing
                    }
                },
                onDelete = {
                    deleteUserFromFirestore(user, firestore) {
                        selectedUser.value = null
                        refreshUsers() // Refresh the users' list after deleting
                    }
                }
            )
        }
    }
}

@Composable
fun AddUserDialog(
    onDismiss: () -> Unit,
    onSubmit: (User) -> Unit,
    name: MutableState<String>,
    email: MutableState<String>,
    password: MutableState<String>
) {
    val errorMessage = remember { mutableStateOf("") }

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
                if (errorMessage.value.isNotEmpty()) {
                    Text(
                        text = errorMessage.value,
                        color = Color.Red,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!email.value.contains('@') || !email.value.endsWith("gmail.com")) {
                        errorMessage.value = "Invalid email! Must be a valid Gmail address."
                    } else if (name.value.isBlank() || password.value.isBlank()) {
                        errorMessage.value = "All fields must be filled."
                    } else {
                        errorMessage.value = ""
                        onSubmit(User(name.value, email.value, password.value))
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
    val errorMessage = remember { mutableStateOf("") }
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
                if (errorMessage.value.isNotEmpty()) {
                    Text(
                        text = errorMessage.value,
                        color = Color.Red,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (!email.value.contains('@') || !email.value.endsWith("gmail.com")) {
                    errorMessage.value = "Invalid email! Must be a valid Gmail address."
                } else if (name.value.isBlank() || password.value.isBlank()) {
                    errorMessage.value = "All fields must be filled."
                } else {
                    errorMessage.value = ""
                    onUpdate(User(name.value, email.value, password.value))
                }
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


// Functions to handle Firestore operations
fun updateUserInFirestore(
    originalEmail: String,
    updatedUser: User,
    firestore: FirebaseFirestore,
    onComplete: () -> Unit
) {
    firestore.collection("users")
        .whereEqualTo("email", originalEmail)
        .get()
        .addOnSuccessListener { result ->
            val document = result.documents.firstOrNull()
            if (document != null) {
                firestore.collection("users").document(document.id)
                    .set(
                        hashMapOf(
                            "name" to updatedUser.name,
                            "email" to updatedUser.email,
                            "password" to updatedUser.password,
                        )
                    )
                    .addOnSuccessListener { onComplete() }
                    .addOnFailureListener { println("Error updating user: ${it.message}") }
            }
        }
        .addOnFailureListener { println("Error fetching user for update: ${it.message}") }
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
                    .addOnSuccessListener { onComplete() }
                    .addOnFailureListener { println("Error deleting user: ${it.message}") }
            }
        }
}


