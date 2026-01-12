package kce.skala.youtubemusicremote

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import coil.load

class MainActivity : AppCompatActivity() {
    private lateinit var vm: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.navigationBarColor = Color.BLACK

        vm = ViewModelProvider(this)[MainViewModel::class.java]

        val etIp = findViewById<EditText>(R.id.et_ip)
        val seekVolume = findViewById<SeekBar>(R.id.seek_volume)
        val seekProgress = findViewById<SeekBar>(R.id.seek_progress)

        // Načtení IP
        val prefs = getSharedPreferences("ytm_prefs", Context.MODE_PRIVATE)
        etIp.setText(prefs.getString("last_ip", "192.168.0.104"))

        vm.isConnected.observe(this) { connected ->
            if (connected) {
                findViewById<View>(R.id.layout_connect).visibility = View.GONE
                hideKeyboard()
                prefs.edit().putString("last_ip", vm.pcIp).apply()
            }
        }

        vm.currentSong.observe(this) { song ->
            song?.let {
                findViewById<TextView>(R.id.tv_title).text = it.title
                findViewById<TextView>(R.id.tv_artist).text = it.artist
                findViewById<ImageView>(R.id.iv_album_art).load(it.imageSrc)
                findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.btn_play_pause)
                    .setImageResource(if (it.isPaused) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause)

                seekProgress.max = it.songDuration
                seekProgress.progress = it.elapsedSeconds
                findViewById<TextView>(R.id.tv_time_elapsed).text = formatTime(it.elapsedSeconds)
                findViewById<TextView>(R.id.tv_time_total).text = formatTime(it.songDuration)
            }
        }

        // --- SYNCHRONIZACE HLASITOSTI ---
        vm.currentVolume.observe(this) { vol ->
            seekVolume.progress = vol
        }

        seekVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var lastSentValue = -1

            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                if (fromUser && Math.abs(p - lastSentValue) > 2) { // Posílat jen při změně o víc než 2%
                    vm.setVolumeOptimistic(p)
                    lastSentValue = p
                }
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {
                s?.let { vm.setVolumeOptimistic(it.progress) }
            }
        })

        // --- PŘIPOJENÍ A OSTATNÍ ---
        findViewById<Button>(R.id.btn_connect).setOnClickListener {
            vm.sendOflineIp(etIp.text.toString())
            vm.login()
        }

        findViewById<View>(R.id.btn_play_pause).setOnClickListener { vm.togglePlayOptimistic() }
        findViewById<View>(R.id.btn_next).setOnClickListener { vm.sendCommand { api, t -> api.nextSong(t) } }
        findViewById<View>(R.id.btn_prev).setOnClickListener { vm.sendCommand { api, t -> api.previousSong(t) } }

        seekProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                if (fromUser) findViewById<TextView>(R.id.tv_time_elapsed).text = formatTime(p)
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {
                s?.let { vm.sendCommand { api, t -> api.seekTo(t, SeekRequest(it.progress)) } }
            }
        })
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    private fun formatTime(s: Int) = String.format("%d:%02d", s / 60, s % 60)
}