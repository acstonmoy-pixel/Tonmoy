package com.example.data.api

import com.example.data.model.Voter
import retrofit2.Response
import retrofit2.http.*

interface SupabaseApi {

    @GET("voters")
    suspend fun getVoters(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorizationHeader: String,
        @Query("select") select: String = "*"
    ): Response<List<Voter>>

    @POST("voters")
    @Headers("Prefer: return=representation")
    suspend fun insertVoters(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorizationHeader: String,
        @Body voters: List<Voter>
    ): Response<List<Voter>>

    @POST("voters")
    @Headers("Prefer: return=representation")
    suspend fun insertVoter(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorizationHeader: String,
        @Body voter: Voter
    ): Response<List<Voter>>
}
