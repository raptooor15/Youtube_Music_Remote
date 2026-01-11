package kce.skala.youtubemusicremote

import androidx.compose.runtime.*
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainViewModel : ViewModel() {
    var token by mutableStateOf("")
    var pcIp by mutableStateOf("192.168.0.100") // Uprav si na svou IP

    private fun getApi(): YtmApi {
        return Retrofit.Builder()
            .baseUrl("http://$pcIp:9863/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(YtmApi::class.java)
    }

    fun login() {
        viewModelScope.launch {
            try {
                val response = getApi().authenticate("MobilniOvladac")
                token = "Bearer ${response.accessToken}"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun send(command: suspend (YtmApi, String) -> Unit) {
        viewModelScope.launch {
            try {
                command(getApi(), token)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}