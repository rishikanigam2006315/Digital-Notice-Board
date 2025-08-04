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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class Event(
    val id: String = "",
    val name: String = "",
    val date: String = "",
    val description: String = "")


class EventViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().getReference("events")
    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = _events

    init{
        fetchEvents()
    }
    private fun fetchEvents(){
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val eventList = mutableListOf<Event>()
               // _events.clear()
                for(itemSnapshot in snapshot.children){
                    val event = itemSnapshot.getValue(Event::class.java)
                    if(event != null){
                        eventList.add(event)
                       // _events.add(event)
                    }
                }
                _events.value = eventList
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
fun addEvent(name: String, date: String, description: String) {
    val id = database.push().key ?: return
    val newEvent = Event(id, name, date, description)
    database.child(id).setValue(newEvent)
}

    fun removeEvent(event: Event) {
        database.child(event.id).removeValue()
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventScreen(navController: NavController,role: String, eventViewModel: EventViewModel = viewModel()) {

    val backgroundImage = painterResource(id = R.drawable.background_image)

    var showDialog by remember { mutableStateOf(false) }
    var eventName by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }
    var eventDesc by remember { mutableStateOf("") }

    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current

    val events by eventViewModel.events.observeAsState(emptyList())
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
                    title = { Text("Events",  color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                )
            },

                    floatingActionButton = {
                        if (role == "admin") {
                    FloatingActionButton(onClick = {
                        showDialog = true
                        //navController.navigate("add_event")
                    },
                      //  containerColor = Color(0xFF837DFF)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Event")
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
                if (userRole.value.isEmpty()){
                    Text("Loading role...", color = Color.White)
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()) {
                    items(events) { event ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) Color.White else MaterialTheme.colorScheme.surface,
                                contentColor = if (isDark) Color.Black else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp)
                                    ) {
                                        Text(
                                            text = event.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.Black
                                        )
                                        Text(
                                            text = event.date,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Black
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = event.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.Black
                                        )
                                    }
                                    if (userRole.value == "admin") {
                                        IconButton(onClick = {
                                            eventViewModel.removeEvent(event)
                                        }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete",
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
        }
    }

    if (showDialog && userRole.value == "admin"){
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    if(eventName.isNotBlank() && eventDate.isNotBlank()){
                        eventViewModel.addEvent(eventName, eventDate, eventDesc)
                        eventName = ""
                        eventDate = ""
                        eventDesc = ""
                        showDialog = false
                    }
                }){
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false
                }){
                    Text("Cancel")
                }
            },
            title = { Text("Add New Event") },
            text = {
                Column {
                    OutlinedTextField(
                        value = eventName,
                        onValueChange = {eventName = it},
                        label = { Text("Event Name", color = Color.Black) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = eventDate,
                        onValueChange = { eventDate = it },
                        label = { Text("Event Date", color=Color.Black) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = eventDesc,
                        onValueChange = { eventDesc = it },
                        label = { Text("Event Description", color = Color.Black) }
                    )
                }
            }
        )
    }
}

