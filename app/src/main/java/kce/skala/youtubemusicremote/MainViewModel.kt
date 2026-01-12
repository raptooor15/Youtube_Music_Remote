package kce.skala.youtubemusicremote

import androidx.lifecycle.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainViewModel : ViewModel() {
    var pcIp: String = "192.168.0.104"
    var token: String = ""

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
                val response = getApi().authenticate("MobilniOvladac")
                token = "Bearer ${response.accessToken}"
                isConnected.postValue(true) // Schováme připojovací lištu
                startPolling()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                try {
                    val api = getApi()
                    currentSong.postValue(api.getSongInfo(token))
                    // Synchronizujeme hlasitost z PC do mobilu
                    val vol = api.getVolume(token)
                    currentVolume.postValue(vol.state)
                } catch (e: Exception) { /* Chyba spojení */ }
                delay(2000)
            }
        }
    }

    fun sendCommand(command: suspend (YtmApi, String) -> Unit) {
        if (token.isEmpty()) return
        viewModelScope.launch {
            try { command(getApi(), token) } catch (e: Exception) { e.printStackTrace() }
        }
    }
}