package com.example.artykul.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

data class Item(val name: String, val room: String, val itemCode: String)

@Composable
fun ItemsList() {
    val firestore = FirebaseFirestore.getInstance()
    val itemsList = remember { mutableStateOf<List<Item>>(emptyList()) }
    val showDialog = remember { mutableStateOf(false) }

    // States for new item input
    val name = remember { mutableStateOf("") }
    val room = remember { mutableStateOf("") }

    // Fetch items from Firestore
    LaunchedEffect(Unit) {
        firestore.collection("items")
            .get()
            .addOnSuccessListener { result ->
                val fetchedItems = result.map { document ->
                    Item(
                        name = document.getString("name") ?: "",
                        room = document.getString("room") ?: "",
                        itemCode = document.getString("itemCode") ?: ""
                    )
                }
                itemsList.value = fetchedItems // Update the list once fetched
            }
            .addOnFailureListener { exception ->
                println("Error fetching items: ${exception.message}")
            }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Items List",
                fontSize = 24.sp,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { showDialog.value = true },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Add Item")
            }
        }

        // Displaying the list of items
        itemsList.value.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(1.dp, Color.Gray)
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Name: ${item.name}",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Room: ${item.room}",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Text(
                    text = "Code: ${item.itemCode}",
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }

        // Dialog for adding a new item
        if (showDialog.value) {
            AddItemDialog(
                onDismiss = { showDialog.value = false },
                onSubmit = { nameValue, roomValue ->
                    addItemToFirestore(
                        name = nameValue,
                        room = roomValue,
                        firestore = firestore
                    ) { newItem ->
                        // Update the list with a copy of the new data
                        itemsList.value = itemsList.value.toMutableList().apply {
                            add(newItem)
                        }
                    }
                    showDialog.value = false
                },
                name = name,
                room = room
            )
        }
    }
}


// Adjusted AddItemToFirestore function to remove master field
fun addItemToFirestore(
    name: String,
    room: String,
    firestore: FirebaseFirestore,
    onComplete: (Item) -> Unit
) {
    val abbreviation = name.take(2).uppercase()
    firestore.collection("items")
        .whereEqualTo("room", room)
        .whereEqualTo("name", name)
        .get()
        .addOnSuccessListener { result ->
            val serialNumber = (result.size() + 1).toString().padStart(3, '0')
            val itemCode = "$abbreviation-$room-$serialNumber"

            val newItem = Item(name, room, itemCode)
            val itemData = hashMapOf(
                "name" to name,
                "room" to room,
                "itemCode" to itemCode
            )

            firestore.collection("items")
                .add(itemData)
                .addOnSuccessListener {
                    println("Item added successfully!")
                    onComplete(newItem)
                }
                .addOnFailureListener { exception ->
                    println("Error adding item: ${exception.message}")
                }
        }
        .addOnFailureListener { exception ->
            println("Error generating item code: ${exception.message}")
        }
}


// Dialog for adding a new item
@Composable
fun AddItemDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit,
    name: MutableState<String>,
    room: MutableState<String>
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Item") },
        text = {
            Column {
                TextField(
                    value = name.value,
                    onValueChange = { name.value = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.padding(8.dp)
                )
                TextField(
                    value = room.value,
                    onValueChange = { room.value = it },
                    label = { Text("Room") },
                    modifier = Modifier.padding(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.value.isNotEmpty() && room.value.isNotEmpty()) {
                        onSubmit(name.value, room.value)
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
