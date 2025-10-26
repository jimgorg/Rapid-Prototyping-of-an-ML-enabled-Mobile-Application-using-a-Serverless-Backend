package com.example.minilens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.nfc.Tag
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.util.Log.d
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.*
import androidx.camera.core.ImageCapture.OutputFileOptions.*
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.processing.SurfaceProcessorNode
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.mutableListOf
import kotlin.coroutines.resume
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


@Composable
fun CameraScreen(context: Context, navController: NavController){

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val scope = rememberCoroutineScope()
    var isCapturing by remember { mutableStateOf(false) }
    var job by remember{mutableStateOf<Job?>(null)}
    val picturesList = remember { mutableListOf<Photo>() }
    var isProcessing by remember{mutableStateOf(false)}



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

                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

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



        fun startPhotoCapturing(time: Duration = 2.seconds){
            val numOfShots: Int = 5

            if(isCapturing) {
                return
            }
            isCapturing =true
            job = scope.launch {
//                Toast.makeText(
//                    context, "OPERATION STARTED", Toast.LENGTH_SHORT).show()
                for(i in 1..numOfShots){
                    if(!isCapturing)
                        break

                    imageCapture?.let{ it->
                        val success = TakePicture(context, it, executor,i){ file, fileBase64 ->
                            picturesList.add(Photo(file.name, fileBase64))
                        }

                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context, if (success) "Photo : $i saved" else "FAILEDTOSAVE photo $i",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    delay(time)
                }
//                delay(time)
                withContext(Dispatchers.Main){
                    Toast.makeText(context,
                        "FINISTED capturing \n\t Total $numOfShots pictures",
                        Toast.LENGTH_LONG).show()
                }
                for (photo in picturesList) {
                    Log.d("chekingobjects", "pic: ${photo.name} json: ${photo.photoOnBase64.take(50)}...")
                }

                val list = JsonifyImages(picturesList)
                val upload = PicturesForUpload(picturesList)


                try{

                    val resp = Retrofit.api.uploadImages(upload)
                    Log.d("SUCCESS","SUCCESS : ${resp.status}, Message : ${resp.message}")
//                    var attempts = 0
//                    val maxAtt = 5
//                    val wait = 2000L
//                    var success = false
//
//                    while (attempts < maxAtt && !success) {
//                        try {
//                            val procesimages = Retrofit.api.runObjectDetection()
//                            if (procesimages.images.isNotEmpty()) {
//                                urls = getResults.images
//                                success = true
//                                Log.d(
//                                    "RESULTSSCREEN",
//                                    "Response received: ${getResults.images.size} images"
//                                )
//                            } else {
//                                Log.d("RESULTSSCREEN", "Attempt ${attempts + 1}: Failed, retrying...")
//                                delay(wait)
//                            }
//                        } catch (e: Exception) {
//                            Log.e("RESULTSSCREEN", "Attempt ${attempts + 1} FAILED: ${e.message}")
//                            delay(wait)
//                        }
//                        attempts++
//                    }
                    val procesimages = Retrofit.api.runObjectDetection()


                    Log.d("SUCCESS","SUCCESS : ${procesimages.statusCode}, Body : ${procesimages.body}")

                }catch (e: Exception){
                    Log.d("ERROR","Upload FAILED : ${e.message}")
                }

                Log.d("WILL_UPLOAD", list.toString(7))
                isCapturing = false
                job = null

//                isProcessing = true

                navController.navigate(Screen.Result.route)

            }

        }

        fun stopCPhotoCapturing(){
            isCapturing = false
            job?.cancel()
            job = null
        }


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
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                tint = Color.White,
                contentDescription = "BAck",
                modifier = Modifier.size(25.dp)
            )
        }

        IconButton(
            onClick = {
                if(isCapturing){
                    stopCPhotoCapturing()
                    Toast.makeText(context, "Stoped Capturing!",
                        Toast.LENGTH_LONG).show()
                }else{
                    startPhotoCapturing()
                    Toast.makeText(context, "Started Capturing!",
                        Toast.LENGTH_LONG).show()
                }
//                imageCapture?.let { capture ->
//                    TakePicture(context, capture, executor) { success, message ->
//                        android.os.Handler(android.os.Looper.getMainLooper()).post {
//                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
//
//                            if (success) {
//                                Log.d("camera Screen", "Photo saved successfully")
//                            }
//                        }
//                    }
//                } ?: run {
//                    Toast.makeText(context, "Camera is not ready", Toast.LENGTH_LONG).show()
//                }
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

suspend fun TakePicture(context: Context,
                        imageCapture : ImageCapture,
                        executor : ExecutorService,
                        num:Int,
                        onSaved : ((file:File, fileBase64:String)->Unit) ?= null
//                        callback:(Boolean, String)->Unit
): Boolean = suspendCancellableCoroutine{ cont ->
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
    val fileName = "Photo_$num.jpg"

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
//        callback(false, "Gallery access failed")
        return@suspendCancellableCoroutine
    }

    fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
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
                val originalBitmap = BitmapFactory.decodeFile(file.absolutePath)
                val rotated = rotateBitmap(originalBitmap, 90f)
                val stream = file.outputStream()
                rotated.compress(Bitmap.CompressFormat.JPEG,100,stream)
                stream.flush()
                stream.close()

                originalBitmap.recycle()
                rotated.recycle()

                android.media.MediaScannerConnection.scanFile(
                    context,
                    arrayOf(file.absolutePath),
                    arrayOf("image/jpeg"),
                    null
                )

                val bytes = file.readBytes()
                val fileBase64 = Base64.encodeToString(bytes, Base64.DEFAULT)
                onSaved?.invoke(file, fileBase64)
//                callback(true, "Photo saved!")
                cont.resume(true)
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraScreen", "FAILED: ${exception.message}")
//                callback(false, "Save failed")
                cont.resume(false)
            }
        }
    )
}

fun JsonifyImages(pictureList: List<Photo>): JSONObject{
    val json = JSONObject()
    val array = JSONArray()

    pictureList.forEach { it ->
        val photo = JSONObject()
        photo.put("name", it.name)
        photo.put("photoOnBase64", it.photoOnBase64 )
        array.put(photo)
    }
    json.put("images", array)
    return  json
}










