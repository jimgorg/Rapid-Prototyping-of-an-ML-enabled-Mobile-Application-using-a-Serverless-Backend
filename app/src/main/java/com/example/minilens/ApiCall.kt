package com.example.minilens

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

data class PicturesForUpload(
        val picturesList : List<Photo>
        )
data class ApiResponse(
    val message : String,
    val status:String
)

data class Results(
    @SerializedName("annotatedImages") val images : List<String>
)
data class ImageResponse(
    val statusCode: Int,
    val body: String
)
interface ApiCall{
    @POST("upload_pics")
   suspend fun uploadImages(@Body body : PicturesForUpload): ApiResponse


    @GET("object_detection")
    suspend fun runObjectDetection(): ImageResponse

   @GET("get_results")
   suspend fun getRecognizedImages(): Results

   @DELETE("delete_results")
   suspend fun deleteResults(): ImageResponse
}

object Retrofit{
    private const val url ="https://x5hjdodjeh.execute-api.us-east-1.amazonaws.com/"
//    private const val url = "http://54.159.44.178:8080/"

    val api : ApiCall by lazy{
        Retrofit.Builder().baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(ApiCall::class.java)
    }


}