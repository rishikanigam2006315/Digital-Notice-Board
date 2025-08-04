package com.example.digitalnoticeboard

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class MarketPost(
    val item: String = "",
    val type: String = "",
    val description: String = "",
    val key: String = ""
    )

// MarketViewModel.kt
class MarketViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().getReference("market")
    private var _items = mutableStateListOf<MarketPost>()
    val items: List<MarketPost> get() = _items

    init {
        fetchItems()
    }

    private fun fetchItems() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _items.clear()
                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(MarketPost::class.java)
                    if (item != null) _items.add(item)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun addItem(item: String,type: String, description: String) {
        val key = database.push().key ?: return
        val newItem = MarketPost(item = item, type = type, description = description, key = key)
        database.child(key).setValue(newItem)
    }

    fun removeItem(item: MarketPost) {
        database.child(item.key).removeValue()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(navController: NavController,role:String, viewModel: MarketViewModel = viewModel()) {
    val backgroundImage = painterResource(id = R.drawable.background_image)
    val databaseRef = FirebaseDatabase.getInstance().getReference("marketPosts")
    var posts = viewModel.items
    val isDark = isSystemInDarkTheme()

    var showDialog by remember { mutableStateOf(false) }
    var item by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val context = LocalContext.current

   // val actualRole = remember(role) { role.lowercase() }

    //val marketItems by viewModel.marketItems.collectAsState()
    val userRole = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        uid?.let {
            FirebaseDatabase.getInstance().getReference("users")
                .child(uid).child("role")
                .get()
                .addOnSuccessListener { snapshot ->
                    userRole.value = snapshot.value.toString()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        context.applicationContext,
                        "Failed to get role: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    userRole.value = "member" // Fallback
                }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // ✅ Full-screen Background Image
        Image(
            painter = backgroundImage,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )


        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Market", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                                //tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                )
            },
            floatingActionButton = {
                if (role == "admin") {
                    FloatingActionButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Market Item")
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(paddingValues).padding(16.dp)
            ) {
                if(userRole.value == null){
                    Text("Loading role...", color = Color.White)
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(posts) { post ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) Color.White else MaterialTheme.colorScheme.surface,
                                contentColor = if (isDark) Color.Black else MaterialTheme.colorScheme.onSurface
                            )

                        ) {
                            Row(Modifier.padding(16.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "${post.type}: ${post.item}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        post.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Black
                                    )
                                }
                                if (userRole.value == "admin") {
                                    IconButton(onClick = {
                                        viewModel.removeItem(post)
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete",
                                            tint = Color.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add Market Item") },
            text = {
                Column {
                    OutlinedTextField(
                        value = item,
                        onValueChange = { item = it },
                        label = { Text("Item") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val typeOptions = listOf("Buy", "Sell", "Rent")
                    var expanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = type,
                            onValueChange = {},
                            label = { Text("Type") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            typeOptions.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        type = selectionOption
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (item.isNotBlank() && description.isNotBlank() && type.isNotBlank()) {
                        viewModel.addItem(item, type, description)
                        item = ""
                        type = ""
                        description = ""
                        showDialog = false
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

