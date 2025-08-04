package com.example.digitalnoticeboard

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

@Composable
fun MainScreen(navController: NavController) {
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val database = FirebaseDatabase.getInstance().reference

    LaunchedEffect(uid) {
        if (uid != null) {
            database.child("users").child(uid).child("role")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val role = snapshot.getValue(String::class.java)
                        if (role != null) {
                            navController.navigate("dashboard/$role") {
                                popUpTo("main") { inclusive = true }
                            }
                        } else {
                            Toast.makeText(context, "No role found", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            navController.navigate("login/member")
        }
    }
}
