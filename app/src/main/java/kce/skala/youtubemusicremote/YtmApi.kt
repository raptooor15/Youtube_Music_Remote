package kce.skala.youtubemusicremote

import retrofit2.http.*

// TYTO JSOU TY "MODELY PRO JSON" - prostě krabičky na data
data class AuthResponse(val accessToken: String)
data class VolumeRequest(val volume: Int)

// Definice příkazů
interface YtmApi {
    @POST("/auth/{id}")
    suspend fun authenticate(@Path("id") id: String): AuthResponse

    @POST("/api/v1/toggle-play")
    suspend fun togglePlay(@Header("Authorization") token: String)

    @POST("/api/v1/next")
    suspend fun nextSong(@Header("Authorization") token: String)

    @POST("/api/v1/previous")
    suspend fun previousSong(@Header("Authorization") token: String)

    @POST("/api/v1/volume")
    suspend fun setVolume(@Header("Authorization") token: String, @Body body: VolumeRequest)
}