package kce.skala.youtubemusicremote


import retrofit2.http.*

data class VolumeRequest(val volume: Int)

interface YtmApi {
    // Teď už neposíláme žádný Header @Header("Authorization")
    @POST("/api/v1/toggle-play")
    suspend fun togglePlay()

    @POST("/api/v1/next")
    suspend fun nextSong()

    @POST("/api/v1/previous")
    suspend fun previousSong()

    @POST("/api/v1/volume")
    suspend fun setVolume(@Body body: VolumeRequest)
}