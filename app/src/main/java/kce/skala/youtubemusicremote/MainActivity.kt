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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load

class MainActivity : AppCompatActivity() {
    private lateinit var vm: MainViewModel
    private lateinit var qAdapter: QueueAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.navigationBarColor = Color.BLACK

        vm = ViewModelProvider(this)[MainViewModel::class.java]

        // --- IP PAMĚŤ ---
        val prefs = getSharedPreferences("ytm_prefs", Context.MODE_PRIVATE)
        val etIp = findViewById<EditText>(R.id.et_ip)
        etIp.setText(prefs.getString("last_ip", ""))

        // --- UP NEXT ---
        val rvQueue = findViewById<RecyclerView>(R.id.rv_queue)
        qAdapter = QueueAdapter()
        rvQueue.layoutManager = LinearLayoutManager(this)
        rvQueue.adapter = qAdapter

        setupObservers(prefs)
        setupListeners()
    }

    private fun setupObservers(prefs: android.content.SharedPreferences) {
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

                val seekProgress = findViewById<SeekBar>(R.id.seek_progress)
                seekProgress.max = it.songDuration
                seekProgress.progress = it.elapsedSeconds

                // Oprava zobrazení času
                findViewById<TextView>(R.id.tv_time_elapsed).text = formatTime(it.elapsedSeconds)
                findViewById<TextView>(R.id.tv_time_total).text = formatTime(it.songDuration)
            }
        }

        vm.queueItems.observe(this) { items ->
            qAdapter.submitList(items)
        }

        vm.currentVolume.observe(this) { vol ->
            findViewById<SeekBar>(R.id.seek_volume).progress = vol
        }
    }

    private fun setupListeners() {
        findViewById<Button>(R.id.btn_connect).setOnClickListener {
            vm.pcIp = findViewById<EditText>(R.id.et_ip).text.toString()
            vm.login()
        }

        vm.queueItems.observe(this) { items ->
            // Toto pošle ty testovací (nebo reálné) songy do adaptéru
            qAdapter.submitList(items)
        }

        // Ovládání času (Seek)
        findViewById<SeekBar>(R.id.seek_progress).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                if (fromUser) findViewById<TextView>(R.id.tv_time_elapsed).text = formatTime(p)
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {
                s?.let { vm.sendCommand { api, t -> api.seekTo(t, SeekRequest(it.progress)) } }
            }
        })

        // Ovládání hlasitosti
        findViewById<SeekBar>(R.id.seek_volume).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                if (fromUser) vm.setVolumeOptimistic(p)
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })

        findViewById<View>(R.id.btn_play_pause).setOnClickListener { vm.togglePlayOptimistic() }
        findViewById<View>(R.id.btn_next).setOnClickListener { vm.sendCommand { api, t -> api.nextSong(t) } }
        findViewById<View>(R.id.btn_prev).setOnClickListener { vm.sendCommand { api, t -> api.previousSong(t) } }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    private fun formatTime(s: Int) = String.format("%d:%02d", s / 60, s % 60)
}