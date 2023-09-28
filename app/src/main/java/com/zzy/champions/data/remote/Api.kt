package com.zzy.champions.data.remote

import com.zzy.champions.data.model.ChampionDetailResponse
import com.zzy.champions.data.model.ChampionResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface Api {

    @GET("/api/versions.json")
    suspend fun getVersions(): List<String>

    @GET("cdn/languages.json")
    suspend fun getSupportLanguage(): List<String>

    @GET("/cdn/{version}/data/{language}/champion.json")
    suspend fun getChampions(@Path("version") version: String, @Path("language") language: String): ChampionResponse

    @GET("/cdn/{version}/data/{language}/champion/{id}.json")
    suspend fun getChampionDetail(@Path("version") version: String, @Path("language") language: String, @Path("id") id: String): ChampionDetailResponse
}