package com.example.data.api

import com.example.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

data class AppConfig(
    val outerLogoUrl: String = "",
    val innerLogoUrl: String = "",
    val creator1Name: String = "بودا العشموني",
    val creator1Photo: String = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400",
    val creator2Name: String = "أحمد طارق",
    val creator2Photo: String = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=400"
)

interface AppApi {
    @GET("profiles.json")
    suspend fun getProfiles(): Map<String, Profile>?

    @PUT("profiles/{id}.json")
    suspend fun uploadProfile(@Path("id") id: Int, @Body profile: Profile): Profile

    @GET("posts.json")
    suspend fun getPosts(): Map<String, CommunityPost>?

    @PUT("posts/{id}.json")
    suspend fun uploadPost(@Path("id") id: Int, @Body post: CommunityPost): CommunityPost

    @GET("comments.json")
    suspend fun getComments(): Map<String, Comment>?

    @PUT("comments/{id}.json")
    suspend fun uploadComment(@Path("id") id: Int, @Body comment: Comment): Comment

    @GET("messages.json")
    suspend fun getMessages(): Map<String, Message>?

    @PUT("messages/{id}.json")
    suspend fun uploadMessage(@Path("id") id: Int, @Body message: Message): Message

    @GET("config.json")
    suspend fun getAppConfig(): AppConfig?

    @PUT("config.json")
    suspend fun uploadAppConfig(@Body config: AppConfig): AppConfig

    companion object {
        private const val BASE_URL = "https://mansouria-together-default-rtdb.firebaseio.com/"

        private val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val instance: AppApi by lazy {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            retrofit.create(AppApi::class.java)
        }
    }
}
