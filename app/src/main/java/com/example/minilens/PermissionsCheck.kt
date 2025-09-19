package com.example.minilens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

//Check if we have perimission

fun CheckForPermissions(context: Context): Boolean{
    if(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA ) == PERMISSION_GRANTED
        && (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PERMISSION_GRANTED)
//            &&(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED)
        )
    {
        return true

    }else{
        return false
    }
}

