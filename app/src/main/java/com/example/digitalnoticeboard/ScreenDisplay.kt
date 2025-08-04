package com.example.digitalnoticeboard

sealed class ScreenDisplay(val route: String){
    object Home: ScreenDisplay("home")
    object Announcements : ScreenDisplay("announcements")
    object Events : ScreenDisplay("events")
    object Market : ScreenDisplay("market")
    object Contacts : ScreenDisplay("contacts")
    object AdminLogin : ScreenDisplay("admin_login")
}