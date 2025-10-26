package com.example.minilens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun Navigation(context: Context, navController : NavHostController = rememberNavController()){
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route)
    {
        composable(route = Screen.Home.route){
            HomeScreen(context = context, navController = navController)
        }
        composable(route = Screen.Camera.route ){
            CameraScreen(context = context, navController = navController)
        }
        composable(route= Screen.Result.route) {
            ResultsScreen(context= context, navController = navController)
        }

    }

}