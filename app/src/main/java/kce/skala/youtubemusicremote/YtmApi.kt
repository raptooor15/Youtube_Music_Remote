package kce.skala.youtubemusicremote

import retrofit2.http.*

data class SongInfo(
    val title: String,
    val artist: String,
    val album: String?,
    val imageSrc: String?,
    val isPaused: Boolean,
    val songDuration: Int,
    val elapsedSeconds: Int,
    val videoId: String?,
    val mediaType: String?
)

data class AuthResponse(val accessToken: String)
data class VolumeRequest(val volume: Int)
data class VolumeResponse(val state: Int, val isMuted: Boolean)
// Model pro posun v čase
data class SeekRequest(val seconds: Int)

interface YtmApi {
    @POST("/auth/{id}")
    suspend fun authenticate(@Path("id") id: String): AuthResponse

    @GET("/api/v1/song")
    suspend fun getSongInfo(@Header("Authorization") token: String): SongInfo

    @GET("/api/v1/volume")
    suspend fun getVolume(@Header("Authorization") token: String): VolumeResponse

    @POST("/api/v1/toggle-play")
    suspend fun togglePlay(@Header("Authorization") token: String)

    @POST("/api/v1/next")
    suspend fun nextSong(@Header("Authorization") token: String)

    @POST("/api/v1/previous")
    suspend fun previousSong(@Header("Authorization") token: String)

    @POST("/api/v1/volume")
    suspend fun setVolume(@Header("Authorization") token: String, @Body body: VolumeRequest)

    // Přidáno: Posun v čase
    @POST("/api/v1/seek-to")
    suspend fun seekTo(@Header("Authorization") token: String, @Body body: SeekRequest)
}