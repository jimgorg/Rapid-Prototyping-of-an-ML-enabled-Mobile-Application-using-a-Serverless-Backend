package com.example.minilens

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun HomeScreen(context: Context,
               navController: NavController
){

    //request persmissions if needed
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {
            //i have persmission
            if( it[Manifest.permission.CAMERA] ==true
                && it[Manifest.permission.READ_MEDIA_IMAGES] == true)
                {
                navController.navigate(Screen.Camera.route)
                } else { //request Permission
                    val rationalRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                        context as MainActivity, Manifest.permission.CAMERA)
//                            || ActivityCompat.shouldShowRequestPermissionRationale(
//                        context as MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                            || ActivityCompat.shouldShowRequestPermissionRationale(
                        context as MainActivity, Manifest.permission.READ_MEDIA_IMAGES)

                    if(rationalRequired){
                        Toast.makeText(context, "Camera perssmission is required. ", Toast.LENGTH_LONG).show()
                    }else{
                        Toast.makeText(context, "Access Denied !", Toast.LENGTH_LONG).show()
                    }
                }



            }
        )


    Box(modifier = Modifier.fillMaxSize().background(colorResource(R.color.white)),
        contentAlignment = Alignment.Center,

    ) {

        Column(modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            Spacer(modifier = Modifier.height(150.dp))

            Image(
                painter = painterResource(id = R.drawable.shutter),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(300.dp)
//                    .padding(bottom = 15.dp, top = 0.dp)
            )
            Text("Welcome to ",
//                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 50.sp,
                color = colorResource(R.color.red),
                modifier = Modifier.padding(top = 50.dp),
                fontStyle = FontStyle.Italic,
                fontFamily = FontFamily.Cursive
            )

            Text(" Mini Lens ",
//                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 80.sp,
                color = colorResource(R.color.red),
                modifier = Modifier.padding(top = 5.dp),
                fontStyle = FontStyle.Italic,
                fontFamily = FontFamily.Cursive
            )

            Spacer(modifier = Modifier.height(80.dp))

            Button(
                modifier = Modifier.width(300.dp).height(70.dp)
                    .align(alignment = Alignment.CenterHorizontally,),
                colors= ButtonDefaults.buttonColors(colorResource(id = R.color.red)),

                onClick = {
                    //Permissions granted
                    if(CheckForPermissions(context)){
                       navController.navigate(Screen.Camera.route)
                    }else{
                        requestPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.READ_MEDIA_IMAGES,
                            )
                        )
                    }
                }
            ) {
                Text(text = "Shoot",
                    textAlign = TextAlign.Center,
                    color = colorResource(id = R.color.white),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }





        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    // Fake context and NavController for preview
    val context = LocalContext.current
    val navController = rememberNavController()

    HomeScreen(context = context, navController = navController)
}