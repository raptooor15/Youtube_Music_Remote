package kce.skala.youtubemusicremote


import androidx.compose.runtime.*
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainViewModel : ViewModel() {
    var pcIp by mutableStateOf("192.168.0.104")
    var statusMessage by mutableStateOf("Připraveno")

    private fun getApi(): YtmApi {
        return Retrofit.Builder()
            .baseUrl("http://$pcIp:9863/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(YtmApi::class.java)
    }

    fun send(command: suspend (YtmApi) -> Unit) {
        viewModelScope.launch {
            try {
                statusMessage = "Odesílám..."
                command(getApi())
                statusMessage = "Úspěch!"
            } catch (e: Exception) {
                statusMessage = "Chyba: ${e.localizedMessage}"
            }
        }
    }
}