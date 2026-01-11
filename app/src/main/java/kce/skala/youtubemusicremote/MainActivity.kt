package kce.skala.youtubemusicremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kce.skala.youtubemusicremote.ui.theme.YoutubeMusicRemoteTheme

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RemoteControlScreen()
                }
            }
        }
    }
}

@Composable
fun RemoteControlScreen(vm: MainViewModel = viewModel()) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(value = vm.pcIp, onValueChange = { vm.pcIp = it }, label = { Text("IP PC") })

        Button(onClick = { vm.login() }, modifier = Modifier.padding(8.dp)) {
            Text(if (vm.token.isEmpty()) "Připojit (Získat Token)" else "Připojeno ✅")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row {
            Button(onClick = { vm.send { api, t -> api.previousSong(t) } }) { Text("<<") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { vm.send { api, t -> api.togglePlay(t) } }) { Text("Play/Pause") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { vm.send { api, t -> api.nextSong(t) } }) { Text(">>") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Hlasitost")
        Slider(
            value = 50f,
            onValueChange = { vm.send { api, t -> api.setVolume(t, VolumeRequest(it.toInt())) } },
            valueRange = 0f..100f
        )
    }
}