package kce.skala.youtubemusicremote

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import coil.load

class MainActivity : AppCompatActivity() {
    private lateinit var vm: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vm = ViewModelProvider(this)[MainViewModel::class.java]

        val layoutConnect = findViewById<LinearLayout>(R.id.layout_connect)
        val btnPlay = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.btn_play_pause)
        val seekVolume = findViewById<SeekBar>(R.id.seek_volume)

        // 1. SKRÝVÁNÍ PŘIPOJENÍ
        vm.isConnected.observe(this) { connected ->
            layoutConnect.visibility = if (connected) View.GONE else View.VISIBLE
        }

        // 2. LOGIKA PLAY/PAUSE IKONY
        vm.currentSong.observe(this) { song ->
            song?.let {
                findViewById<TextView>(R.id.tv_title).text = it.title
                findViewById<TextView>(R.id.tv_artist).text = it.artist
                findViewById<ImageView>(R.id.iv_album_art).load(it.imageSrc)

                // Pokud je PAUZNUTO, zobrazíme ikonu PLAY (trojúhelník)
                // Pokud HRAJE, zobrazíme ikonu PAUSE (dvě čárky)
                if (it.isPaused) {
                    btnPlay.setImageResource(android.R.drawable.ic_media_play)
                } else {
                    btnPlay.setImageResource(android.R.drawable.ic_media_pause)
                }
            }
        }

        // 3. AKTUALIZACE SLIDERU Z PC DO MOBILU
        vm.currentVolume.observe(this) { vol ->
            seekVolume.progress = vol
        }

        // POSÍLÁNÍ HLASITOSTI Z MOBILU DO PC
        seekVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                if (fromUser) { // Pouze pokud hýbe uživatel prstem
                    vm.sendCommand { api, t -> api.setVolume(t, VolumeRequest(p)) }
                }
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })

        findViewById<Button>(R.id.btn_connect).setOnClickListener {
            vm.pcIp = findViewById<EditText>(R.id.et_ip).text.toString()
            vm.login()
        }

        btnPlay.setOnClickListener { vm.sendCommand { api, t -> api.togglePlay(t) } }
        findViewById<ImageButton>(R.id.btn_next).setOnClickListener { vm.sendCommand { api, t -> api.nextSong(t) } }
        findViewById<ImageButton>(R.id.btn_prev).setOnClickListener { vm.sendCommand { api, t -> api.previousSong(t) } }
    }
}