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

    // TEST: Inicializujeme seznam rovnou s daty
    val queueItems = MutableLiveData<List<SongInfo>>(listOf(
        SongInfo("Testovací Skladba 1", "Zkušební Umělec", "Album", null, false, 240, 0),
        SongInfo("Druhá Skladba", "Někdo Jiný", "Album", null, false, 180, 0)
    ))

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
                isConnected.postValue(true)
                startPolling()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                try {
                    val api = getApi()
                    currentSong.postValue(api.getSongInfo(token))

                    // PRO REÁLNÝ TEST: Zakomentuj tento řádek, pokud chceš vidět jen ty testovací songy
                    // val q = api.getQueue(token)
                    // queueItems.postValue(q.items)

                    if (System.currentTimeMillis() - lastVolumeChangeTime > 2000) {
                        currentVolume.postValue(api.getVolume(token).state)
                    }
                } catch (e: Exception) { }
                delay(1000)
            }
        }
    }

    // ... zbytek funkcí zůstává stejný ...
    fun setVolumeOptimistic(v: Int) { /*...*/ }
    fun togglePlayOptimistic() { /*...*/ }
    fun sendCommand(c: suspend (YtmApi, String) -> Unit) { /*...*/ }
}