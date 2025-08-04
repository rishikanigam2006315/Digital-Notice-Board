package com.example.digitalnoticeboard

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


data class Announcement(
    val id: String = "",
    val title: String = "",
    val description: String = "")

class AnnouncementViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().getReference("announcements")
    private var _announcements = mutableStateListOf<Announcement>()
    val announcements: List<Announcement> get() = _announcements

    init {
        fetchAnnouncements()
    }

    private fun fetchAnnouncements() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _announcements.clear()
                for (itemSnapshot in snapshot.children) {
                    val announcement = itemSnapshot.getValue(Announcement::class.java)
                    if (announcement != null) {
                        _announcements.add(announcement)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching announcements: ${error.message}")
            }
        })
    }

    fun addAnnouncement(title: String, description: String) {
        val id = database.push().key ?: return
        val newItem = Announcement(id, title, description)
        database.child(id).setValue(newItem)
    }

    fun removeAnnouncement(announcement: Announcement) {
        database.child(announcement.id).removeValue()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementScreen(navController: NavController, role: String, viewModel: AnnouncementViewModel = viewModel()) {
    val backgroundImage = painterResource(id = R.drawable.background_image)
   // val database = FirebaseDatabase.getInstance().getReference("announcements")

    var showDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newDesc by remember { mutableStateOf("") }

    val isDark = isSystemInDarkTheme()

   // val announcements = remember { mutableStateListOf<Announcement>() }

    val context = LocalContext.current
    //val actualRole = remember(role) { role.lowercase() }
   // val announcements by viewModel.announcements.collectAsState()
    val userRole = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("ROLE_CHECK", "UID: $uid")
        uid?.let {
            FirebaseDatabase.getInstance().getReference("users")
                .child(uid).child("role")
                .get()
                .addOnSuccessListener { snapshot ->
                    val fetchedRole = snapshot.value.toString()
                    Log.d("ROLE_CHECK", "Fetched role: $fetchedRole")
                    userRole.value = fetchedRole
                }
                .addOnFailureListener { e ->
                    Log.d("ROLE_CHECK", "Failed to fetch role: ${e.message}")
                    userRole.value = "member" // fallback
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
                title = { Text("Announcements",  color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
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
            )
        },
        floatingActionButton = {
            if (role == "admin"){
                FloatingActionButton(onClick = {
//                    navController.navigate("AddAnnouncementScreen")
//                }){
                    showDialog = true }){
                    Icon(Icons.Default.Add, contentDescription = "Add Announcement")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {


            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(viewModel.announcements) { announcement ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color.White else MaterialTheme.colorScheme.surface,
                            contentColor = if (isDark) Color.Black else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                Modifier.weight(1f)
                            ) {
                                Text(
                                    announcement.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    announcement.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black
                                )
                            }
                            if (userRole.value == "admin") {
                                IconButton(onClick = {
                                    viewModel.removeAnnouncement(announcement)
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete",
                                        tint = Color.Black
                                    )
                                }
                            }
                        }

                    }
                }
            }
        }
    }

        if (showDialog){
            AlertDialog(
                onDismissRequest = {showDialog = false},
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.addAnnouncement(newTitle, newDesc)
                            newTitle = ""
                            newDesc = ""
                            showDialog = false

                    }){
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }){
                        Text("Cancel")
                    }
                },
                title = { Text("Add Announcement") },
                text = {
                    Column {
                        OutlinedTextField(value = newTitle, onValueChange = {newTitle = it}, label = { Text("Title") })
                        OutlinedTextField(value = newDesc, onValueChange = {newDesc = it}, label = { Text("Description") })
                    }
                }
            )
        }
    }
}