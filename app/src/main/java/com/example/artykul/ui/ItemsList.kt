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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

data class Item(val name: String, val room: String, val itemCode: String)



@Composable
fun ItemsList() {
    val firestore = FirebaseFirestore.getInstance()
    val itemsList = remember { mutableStateOf<List<Item>>(emptyList()) }
    val selectedItem = remember { mutableStateOf<Item?>(null) }
    val showEditDialog = remember { mutableStateOf(false) }
    val showAddDialog = remember { mutableStateOf(false) }

    // Fetch items from Firestore
    LaunchedEffect(Unit) {
        fetchItemsFromFirestore(firestore) { fetchedItems ->
            itemsList.value = fetchedItems.sortedBy { it.room }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Items List",
                fontSize = 24.sp,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { showAddDialog.value = true },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Add Item")
            }
        }


    LazyColumn(modifier = Modifier.weight(1f)) {
        items(itemsList.value) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(1.dp, Color.Gray)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Name: ${item.name}", fontSize = 18.sp)
                    Text(text = "Room: ${item.room}", fontSize = 16.sp)
                    Text(text = "Code: ${item.itemCode}", fontSize = 16.sp)
                }
                Button(onClick = {
                    selectedItem.value = item
                    showEditDialog.value = true
                }) {
                    Text("Edit")
                }
            }
        }
    }

        // Add Item Dialog
        if (showAddDialog.value) {
            AddItemDialog(
                onDismiss = { showAddDialog.value = false },
                onSubmit = { name, room ->
                    addItemToFirestore(
                        name = name,
                        room = room,
                        firestore = firestore
                    ) { newItem ->
                        itemsList.value = itemsList.value.toMutableList().apply {
                            add(newItem)
                        }
                        showAddDialog.value = false
                    }
                }
            )
        }

        // Edit Item Dialog
        if (showEditDialog.value && selectedItem.value != null) {
            EditItemDialog(
                item = selectedItem.value!!,
                onDismiss = { showEditDialog.value = false },
                onUpdate = { updatedItem ->
                    updateItemInFirestore(updatedItem, firestore) {
                        itemsList.value = itemsList.value.map {
                            if (it.itemCode == updatedItem.itemCode) updatedItem else it
                        }
                        showEditDialog.value = false
                    }
                },
                onDelete = { itemToDelete ->
                    deleteItemFromFirestore(itemToDelete, firestore) {
                        itemsList.value = itemsList.value.filter { it.itemCode != itemToDelete.itemCode }
                        showEditDialog.value = false
                    }
                }
            )
        }
    }
}

@Composable
fun AddItemDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    val name = remember { mutableStateOf("") }
    val room = remember { mutableStateOf("") }

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
                    onValueChange = { if (it.length <= 3) room.value = it },
                    label = { Text("Room (3 digits)") },
                    modifier = Modifier.padding(8.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.value.isNotEmpty() && room.value.isNotEmpty()) {
                    onSubmit(name.value, room.value)
                }
            }) {
                Text("Add")
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
fun EditItemDialog(
    item: Item,
    onDismiss: () -> Unit,
    onUpdate: (Item) -> Unit,
    onDelete: (Item) -> Unit
) {
    val name = remember { mutableStateOf(item.name) }
    val room = remember { mutableStateOf(item.room) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Item") },
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
                    onValueChange = { if (it.length <= 3) room.value = it },
                    label = { Text("Room (3 digits)") },
                    modifier = Modifier.padding(8.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.value.isNotEmpty() && room.value.isNotEmpty()) {
                    val updatedItem = item.copy(name = name.value, room = room.value)
                    onUpdate(updatedItem)
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Column {
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
                Button(onClick = { onDelete(item) }, colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    contentColor = Color.Red
                )) {
                    Text("Delete")
                }
            }
        }
    )
}

fun updateItemInFirestore(
    item: Item,
    firestore: FirebaseFirestore,
    onComplete: () -> Unit
) {
    firestore.collection("items")
        .whereEqualTo("itemCode", item.itemCode)
        .get()
        .addOnSuccessListener { result ->
            val document = result.documents.firstOrNull()
            document?.reference?.update(
                "name", item.name,
                "room", item.room
            )?.addOnSuccessListener {
                println("Item updated successfully!")
                onComplete()
            }?.addOnFailureListener { exception ->
                println("Error updating item: ${exception.message}")
            }
        }
        .addOnFailureListener { exception ->
            println("Error finding item: ${exception.message}")
        }
}

fun deleteItemFromFirestore(
    item: Item,
    firestore: FirebaseFirestore,
    onComplete: () -> Unit
) {
    firestore.collection("items")
        .whereEqualTo("itemCode", item.itemCode)
        .get()
        .addOnSuccessListener { result ->
            val document = result.documents.firstOrNull()
            document?.reference?.delete()
                ?.addOnSuccessListener {
                    println("Item deleted successfully!")
                    onComplete()
                }
                ?.addOnFailureListener { exception ->
                    println("Error deleting item: ${exception.message}")
                }
        }
        .addOnFailureListener { exception ->
            println("Error finding item: ${exception.message}")
        }
}

fun fetchItemsFromFirestore(
    firestore: FirebaseFirestore,
    onComplete: (List<Item>) -> Unit
) {
    firestore.collection("items")
        .get()
        .addOnSuccessListener { result ->
            val items = result.map { document ->
                Item(
                    name = document.getString("name") ?: "",
                    room = document.getString("room") ?: "",
                    itemCode = document.getString("itemCode") ?: ""
                )
            }
            onComplete(items)
        }
        .addOnFailureListener { exception ->
            println("Error fetching items: ${exception.message}")
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

