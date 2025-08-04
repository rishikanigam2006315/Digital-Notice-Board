package com.example.digitalnoticeboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.digitalnoticeboard.ui.theme.DigitalNoticeBoardTheme
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("app_status")
        ref.setValue("Digital Notice Board is connected!")
        setContent {
            DigitalNoticeBoardTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "splash") {
                        composable("splash") { SplashScreen(navController) }
                        composable("role_selection") { RoleSelectionScreen(navController) }
                        composable("login/{role}") { backStack ->
                            val role = backStack.arguments?.getString("role") ?: "member"
                            LoginScreen(navController, role)
                        }

                        composable("main"){
                            MainScreen(navController)
                        }
                        composable(
                            "main/{role}",
                            arguments = listOf(navArgument("role") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val role = backStackEntry.arguments?.getString("role") ?: "member"
                            HomeScreen(navController, role)

                        }
                        composable(
                            "events/{role}",
                            arguments = listOf(navArgument("role") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val role = backStackEntry.arguments?.getString("role") ?: "member"
                            val userRole = remember { mutableStateOf(role) }
                            EventScreen(navController, userRole.value)
                        }
                        composable("announcements/{role}",
                            arguments = listOf(navArgument("role"){ type = NavType.StringType})
                            ) { backStackEntry ->
                            val role = backStackEntry.arguments?.getString("role")?: "member"
                            AnnouncementScreen(navController, role)
                        }
                        composable("market/{role}",
                            arguments = listOf(navArgument("role") { type = NavType.StringType})
                            ) { backStactEntry ->
                            val role = backStactEntry.arguments?.getString("role") ?: "member"
                            MarketScreen(navController, role)
                        }
                        composable("contacts/{role}",
                            arguments = listOf(navArgument("role") {type = NavType.StringType})
                            ) { backStackEntry ->
                            val role = backStackEntry.arguments?.getString("role") ?: "member"
                            ImportantContactsScreen(navController, role)
                        }
                        composable("register") {
                            RegisterScreen(navController)
                        }
                        composable("forgot_password"){
                            ForgotPasswordScreen(navController)
                        }

                    }
                }
            }
        }
    }
}




