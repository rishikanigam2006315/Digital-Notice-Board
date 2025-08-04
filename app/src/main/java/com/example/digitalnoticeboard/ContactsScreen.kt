package com.example.digitalnoticeboard

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class Contact(
    val name: String = "",
    val role: String = "",
    val phone: String = "",
    val email: String = "",
    val key: String = ""
)
class ContactViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().getReference("contacts")
   val contacts = mutableStateListOf<Contact>()


    init {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                contacts.clear()
                for (itemSnapshot in snapshot.children) {
                    val contact = itemSnapshot.getValue(Contact::class.java)
                    if (contact != null) {
                        contacts.add(contact)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error: ${error.message}")
            }
        })
    }

    fun addContact(name: String, role: String, phone: String, email: String) {
        val key = database.push().key ?: return
        val newContact = Contact(
            key = key, name = name, role = role, phone = phone, email = email)
        database.child(key).setValue(newContact)
    }

    fun removeContact(contact: Contact) {
        database.child(contact.key).removeValue()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportantContactsScreen(navController: NavController,role: String, viewModel: ContactViewModel = viewModel()) {
    val backgroundImage = painterResource(id = R.drawable.background_image)
    val contacts = viewModel.contacts
    val isDark = isSystemInDarkTheme()


    var showDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var contactRole by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    val context = LocalContext.current

    //val actualRole = remember(role) { role.lowercase() }

   // val contacts by viewModel.contacts.collectAsState()
    val userRole = remember { mutableStateOf<String?>(null) }

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
        // 🌄 Background Image
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
                    title = { Text("Contacts",  color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                               // tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                )
            },
            floatingActionButton = {
                if (role == "admin") {
                    FloatingActionButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Contact")
                    }
                }
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (userRole.value == null) {
                    Text("Loading role...", color = Color.White)
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(contacts) { contact ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) Color.White else MaterialTheme.colorScheme.surface,
                                contentColor = if (isDark) Color.Black else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Row(Modifier.padding(16.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        contact.name,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        fontSize = 18.sp
                                    )
                                    Text(contact.role, fontSize = 14.sp,  color = Color.Black)// color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(contact.phone, fontSize = 14.sp,  color = Color.Black,// color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.clickable {
                                            var intent = Intent(
                                                Intent.ACTION_DIAL,
                                                Uri.parse("tel:${contact.phone}")
                                            )
                                            context.startActivity(intent)
                                        })
                                    Text(contact.email, fontSize = 14.sp, color = Color.Black,// color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.clickable {
                                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                                data = Uri.parse("mailto:${contact.email}")
                                            }
                                            context.startActivity(intent)
                                        })
                                }
                                if (userRole.value == "admin") {
                                    IconButton(onClick = {
                                        viewModel.removeContact(contact)
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
            title = { Text("Add Contact") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = contactRole, onValueChange = { contactRole = it }, label = { Text("Role") })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        viewModel.addContact(name, contactRole, phone, email)
                        name = ""
                        contactRole = ""
                        phone = ""
                        email = ""
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

