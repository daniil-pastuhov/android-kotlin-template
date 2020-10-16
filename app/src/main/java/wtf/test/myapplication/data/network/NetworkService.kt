package wtf.test.myapplication.data.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import wtf.test.myapplication.data.models.GameModel

class NetworkService {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://create.kahoot.it/")
        .client(OkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    suspend fun getGameApiCall(uuid: String): GameModel = apiService.getGameApiCall(uuid)
}

interface ApiService {
    //fb4054fc-6a71-463e-88cd-243876715bc1
    @GET("rest/kahoots/{uuid}")
    suspend fun getGameApiCall(@Path("uuid") uuid: String) : GameModel
}