package com.example.digitalnoticeboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, role: String) {
    val backgroundImage = painterResource(id = R.drawable.background_image)
    var selectedTabIndex by remember { mutableStateOf(0) }

    Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Image(
                painter = backgroundImage,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Scaffold(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    when (selectedTabIndex) {
                        0 -> AdminLoginContent(navController)
                        1 -> MemberLoginContent(navController)
                    }
                }
            }
                }
            }



@Composable
fun AdminLoginContent(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val error = remember { mutableStateOf("") }

    LoginForm(
        username = email,
        onUsernameChange = { email = it },
        password = password,
        onPasswordChange = { password = it },
        onLoginClick = {
            auth.signInWithEmailAndPassword(email.trim(), password.trim())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid
                        val dbRef = database.getReference("users").child(uid ?: "")
                        dbRef.child("role").get().addOnSuccessListener { snapshot ->
                            if(!snapshot.exists()) {
                                dbRef.child("role").setValue("admin")
                            }
                           // .addOnSuccessListener {
                                navController.navigate("main/admin") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                    } else {
                        error.value = "Login failed: ${task.exception?.message}"
                    }
                }
        },
        error = error,
        navController = navController
    )
}

@Composable
fun MemberLoginContent(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val error = remember { mutableStateOf("") }

    LoginForm(
        username = email,
        onUsernameChange = { email = it },
        password = password,
        onPasswordChange = { password = it },
        onLoginClick = {
            auth.signInWithEmailAndPassword(email.trim(), password.trim())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid
                        val dbRef = database.getReference("users").child(uid ?: "")
                        dbRef.child("role").get().addOnSuccessListener { snapshot ->
                            if (!snapshot.exists()) {
                                dbRef.child("role").setValue("member")
                            }
                                navController.navigate("main/member") {
                                    popUpTo("login") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                    } else {
                        error.value = "Login failed: ${task.exception?.message}"
                    }
                }
        },
        error = error,
        navController = navController
    )
}

@Composable
fun LoginForm(
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    error: MutableState<String>,
    navController: NavController
) {
    Surface(
        color = Color(0xAA000000),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {  }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("Email", color = Color.White) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Cyan,
                unfocusedBorderColor = Color.White,
                cursorColor = Color.Cyan,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        var passwordVisible by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password", color = Color.White) },
            singleLine = true,
            visualTransformation = if(passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }){
                    Icon(
                        imageVector = if(passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if(passwordVisible) "Hide Password" else "Show Password",
                        tint = Color.White
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Cyan,
                unfocusedBorderColor = Color.White,
                cursorColor = Color.Cyan,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DE9B6))
        ) {
            Text("Login")
        }

        if (error.value.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(error.value, color = MaterialTheme.colorScheme.error)
        }
        TextButton(onClick = { navController.navigate("register") }) {
            Text("Don't have an account? Register", color = Color.White)
        }

        TextButton(onClick = { navController.navigate("forgot_password") }) {
            Text("Forgot Password?", color = Color.White)
        }

    }
}






