package com.example.vitalityapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// 1. Data class for the ZenQuotes API
data class QuoteDto(val q: String, val a: String) 

// 2. Retrofit Interface
interface ZenQuotesApi {
    @GET("api/random")
    suspend fun getRandomQuote(): List<QuoteDto>
    
    companion object {
        fun create(): ZenQuotesApi {
            return Retrofit.Builder()
                .baseUrl("https://zenquotes.io/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ZenQuotesApi::class.java)
        }
    }
}

// 3. Data class for Firebase Firestore
data class CommunityPost(
    val id: String = "",
    val userName: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
