package kce.skala.youtubemusicremote

import androidx.lifecycle.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainViewModel : ViewModel() {
    var pcIp: String = ""
    var token: String = ""
    private var lastVolumeChangeTime: Long = 0

    val currentSong = MutableLiveData<SongInfo?>()
    val currentVolume = MutableLiveData<Int>()
    val isConnected = MutableLiveData<Boolean>(false)

    private fun getApi() = Retrofit.Builder()
        .baseUrl("http://$pcIp:9863/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(YtmApi::class.java)

    fun login() {
        viewModelScope.launch {
            try {
                val api = getApi()
                val response = api.authenticate("MobilniOvladac")
                token = "Bearer ${response.accessToken}"
                currentSong.postValue(api.getSongInfo(token))
                currentVolume.postValue(api.getVolume(token).state)
                isConnected.postValue(true)
                startPolling()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun setVolumeOptimistic(newVol: Int) {
        lastVolumeChangeTime = System.currentTimeMillis()
        currentVolume.value = newVol // Okamžitá změna v UI
        sendCommand { api, t -> api.setVolume(t, VolumeRequest(newVol)) }
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                try {
                    val api = getApi()
                    currentSong.postValue(api.getSongInfo(token))

                    // AKTUALIZACE VOLUMU JEN POKUD JE KLID (2 sekundy od poslední změny)
                    if (System.currentTimeMillis() - lastVolumeChangeTime > 2000) {
                        currentVolume.postValue(api.getVolume(token).state)
                    }
                } catch (e: Exception) { }
                delay(1000) // Plynulý progress bar (1s)
            }
        }
    }

    fun togglePlayOptimistic() {
        val current = currentSong.value ?: return
        currentSong.value = current.copy(isPaused = !current.isPaused)
        sendCommand { api, t -> api.togglePlay(t) }
    }

    fun sendOflineIp(ip: String) { this.pcIp = ip }

    fun sendCommand(command: suspend (YtmApi, String) -> Unit) {
        if (token.isEmpty()) return
        viewModelScope.launch {
            try { command(getApi(), token) } catch (e: Exception) { }
        }
    }
}