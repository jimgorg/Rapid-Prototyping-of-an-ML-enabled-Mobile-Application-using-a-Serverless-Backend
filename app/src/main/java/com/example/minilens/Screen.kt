package com.example.minilens

sealed class Screen(val route :String) {
    object Home : Screen("home_screen")
    object  Camera : Screen("camera_screen")
}