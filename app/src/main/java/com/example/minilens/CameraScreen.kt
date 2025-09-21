package com.example.minilens

import android.content.Context
import android.nfc.Tag
import android.os.Environment
import android.util.Log
import android.util.Log.d
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.*
import androidx.camera.core.ImageCapture.OutputFileOptions.*
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraScreen(context: Context, navController: NavController){

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }



    Box (modifier = Modifier.fillMaxSize().background(Color.Black))
    {

        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                //runs when the provider is ready.
                cameraProviderFuture.addListener({

                    val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                    //send camera frames into previewView
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    //ensures photos are oriented correctly
                    imageCapture = ImageCapture.Builder()
                        .setTargetRotation(previewView.display.rotation)
                        .build()

                    cameraProvider.unbindAll()
                    try {
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner, // must be LifecycleOwner
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (exc: Exception) {
                        Log.e("CameraScreen", "Use case binding failed", exc)
                    }

                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )







        IconButton(
            onClick = {
                navController.popBackStack()
            },
            modifier = Modifier.size(70.dp)
                .align(alignment = Alignment.TopStart)
                .padding(start = 16.dp, top =42.dp),
            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.7f)),

            ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                tint = Color.White,
                contentDescription = "BAck",
                modifier = Modifier.size(25.dp)
            )
        }

        IconButton(
            onClick = {
                imageCapture?.let { capture ->
                    TakePicture(context, capture, executor) { success, message ->
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                            if (success) {
                                Log.d("camera Screen", "Photo saved successfully")
                            }
                        }
                    }
                } ?: run {
                    Toast.makeText(context, "Camera is not ready", Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
                .size(90.dp),
            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black)
        ) {
            Icon(
                painterResource(R.drawable.aperture),
                contentDescription = "Capture",
                tint = Color.White,
                modifier = Modifier.size(60.dp)
            )
        }

    }
}

private fun TakePicture(context: Context,
                        imageCapture: ImageCapture,
                        executor : ExecutorService,
                        callback:(Boolean, String)->Unit
){

    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
    val fileName = "MiniLens_$timestamp.jpg"

    Log.d("CameraScreen", "Saving to gallery: $fileName")

    val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    val miniLensDir = File(picturesDir, "MiniLens")

    Log.d("CameraScreen", "Gallery Pictures path : ${miniLensDir.absolutePath}")

    if (!miniLensDir.exists()) {
        val created = miniLensDir.mkdirs()
        Log.d("CameraScreen", "Created MiniLens directory: $created")
    }

    if (!miniLensDir.exists() || !miniLensDir.canWrite()) {
        Log.e("CameraScreen", "Cannot write to gallery directory")
        callback(false, "Gallery access failed")
        return
    }

    val file  = File(miniLensDir, fileName)
    val outputOptions = OutputFileOptions.Builder(file).build()




    imageCapture.takePicture(
        outputOptions,
        executor,
        object : OnImageSavedCallback {
            override fun onImageSaved(output: OutputFileResults) {
                Log.d("CameraScreen", "SUCCESS: Photo saved to ${file.absolutePath}")
                Log.d("CameraScreen", "File size: ${file.length()} bytes")
                //media scan so photo appears in gallery
                android.media.MediaScannerConnection.scanFile(
                    context,
                    arrayOf(file.absolutePath),
                    arrayOf("image/jpeg"),
                    null
                )
                callback(true, "Photo saved!")
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraScreen", "FAILED: ${exception.message}")
                callback(false, "Save failed")
            }
        }
    )
}













