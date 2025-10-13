package com.example.minilens

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.POST

data class PicturesForUpload(
        val picturesList : List<Photo>
        )
data class ApiResponse(
    val message : String,
    val status:String
)

interface ApiCall{
    @POST("upload_pics")
   suspend fun uploadImages(@Body body : PicturesForUpload): ApiResponse
}

object Retrofit{
    private const val url ="https://x5hjdodjeh.execute-api.us-east-1.amazonaws.com/"

    val api : ApiCall by lazy{
        Retrofit.Builder().baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(ApiCall::class.java)
    }


}