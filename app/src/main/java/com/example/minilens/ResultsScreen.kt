package com.example.minilens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(context: Context, navController: NavController) {

    var isProcessing by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    var urls by remember { mutableStateOf(listOf<String>()) }
    var bitmaps by remember { mutableStateOf<List<Bitmap?>>(emptyList()) }
    var selectedImage by remember { mutableStateOf<Bitmap?>(null) }

    fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    LaunchedEffect(Unit) {


        try {
            var attempts = 0
            val maxAtt = 5
            val wait = 2500L
            var success = false

            while (attempts < maxAtt && !success) {
                try {
                    val getResults = Retrofit.api.getRecognizedImages()
                    if (getResults.images.isNotEmpty()) {
                        urls = getResults.images
                        success = true
                        Log.d(
                            "RESULTSSCREEN",
                            "Response received: ${getResults.images.size} images"
                        )
                    } else {
                        Log.d("RESULTSSCREEN", "Attempt ${attempts + 1}: Failed, retrying...")
                        delay(wait)
                    }
                } catch (e: Exception) {
                    Log.e("RESULTSSCREEN", "Attempt ${attempts + 1} FAILED: ${e.message}")
                    delay(wait)
                }
                attempts++
            }

            if (urls.isEmpty()) {
                Log.d("FAILED", "failed ->>>>> images[] is empty")
                isProcessing = false
                return@LaunchedEffect
            }

            val downloadedBitmaps = mutableListOf<Bitmap?>()
            urls.forEachIndexed { index, url ->
                Log.d("RESULTSSCREEN", "Downloading image ${index + 1}...")
                val bitmap = withContext(Dispatchers.IO) {
                    try {
                        val connection = URL(url).openConnection()
                        connection.connectTimeout = 15000
                        connection.readTimeout = 15000
                        connection.connect()
                        val inputStream = connection.getInputStream()
                        val originalBitmap = BitmapFactory.decodeStream(inputStream)

                        if (originalBitmap != null) {
                            rotateBitmap(originalBitmap, 0f)
                        } else {
                            null
                        }



                    } catch (e: Exception) {
                        Log.e("RESULTSSCREEN", "Error downloading image: ${e.message}", e)
                        null
                    }
                }
                downloadedBitmaps.add(bitmap)
            }


            bitmaps = downloadedBitmaps
            isProcessing = false


            urls.forEach { Log.d("RESULTS", it) }
        } catch (e: Exception) {
            Log.d("FAILED", "failed getting results from lambda", e)
            isProcessing = false
        }
    }


        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = colorResource(R.color.white)),
            contentAlignment = Alignment.Center
        ) {

            if (isProcessing == true) {
                Log.d("RESULTSSCREEN", "log-1")


                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally

                ) {
                    Spacer(modifier = Modifier.height(150.dp))

                    Image(
                        painter = painterResource(id = R.drawable.loading_4690214),
                        contentDescription = "loading logo",
                        modifier = Modifier.size(400.dp)
                    )

                    Spacer(modifier = Modifier.height(160.dp))

                    Text(
                        "Loading ....",
                        fontWeight = FontWeight.Bold,
                        fontSize = 60.sp,
                        color = colorResource(R.color.red),
                        modifier = Modifier.padding(top = 50.dp),
                        fontStyle = FontStyle.Normal,
                        fontFamily = FontFamily.Cursive
                    )
                }
            } else {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    "Detected Pictures (${urls.size})",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                val deleteResponse = Retrofit.api.deleteResults()
                                                Log.d("RESULTSSCREEN", "Deleted ")
                                                navController.navigate(Screen.Home.route)
                                            } catch (e: Exception) {
                                                Log.e("RESULTSSCREEN", "Error: ${e.message}")
                                            }
                                        }
                                }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        modifier = Modifier.size(40.dp),
                                        tint = colorResource(R.color.red),
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = colorResource(R.color.white),
//                                titleContentColor = Color.White,
                                titleContentColor = colorResource(R.color.red),
                                navigationIconContentColor = Color.White
                            )
                        )
                    }
                ) { padding ->

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(bitmaps) { index, bitmap ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(
//                                    containerColor = MaterialTheme.colorScheme.
                                    containerColor = colorResource(R.color.trythisgrey)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Image ${index + 1}",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
//                                            color = MaterialTheme.colorScheme.primary
//                                            color = Color.White
                                            color = colorResource(R.color.red)
                                        )

                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(600.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = colorResource(
                                                R.color.black
                                            )
                                        )
                                    ) {
//                                    AsyncImage(
//                                        model = ImageRequest.Builder(context)
//                                            .data(url)
//                                            .crossfade(true)
//                                            .build(),
//                                        contentDescription = "Detected objects in image ${index + 1}",
//                                        modifier = Modifier.fillMaxSize(),
//                                        contentScale = ContentScale.Fit,
//                                        onError = {
//                                            Log.e("RESULTSSCREEN", "Failed to load image $index: ${it.result.throwable}")
//                                        },
//                                        onSuccess = {
//                                            Log.d("RESULTSSCREEN", "Successfully loaded image $index")
//                                        },
//
//                                    )
                                        if (bitmap != null) {
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = "Detected objects in image ${index + 1}",
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clickable { selectedImage = bitmap },
                                                contentScale = ContentScale.Fit,
                                            )
                                        }
                                    }
                                }
                            }

//                             Bottom spacing
//                        item {
//                            Spacer(modifier = Modifier.height(16.dp))
//                        }
                        }
                    }
                }
                selectedImage?.let{
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .clickable {
                            selectedImage = null
                        },
                        contentAlignment = Alignment.Center
                    ){
                        Image(
                            bitmap = selectedImage!!.asImageBitmap(),
                            contentDescription = "fullscreen mode",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

            }

        }
    }






//@Preview(showBackground = true)
//@Composable
//fun PreviewResultsScreen() {
//    // Fake context and NavController for preview
//    val context = LocalContext.current
//    val navController = rememberNavController()
//
//    ResultsScreen(context = context, navController = navController)
//}