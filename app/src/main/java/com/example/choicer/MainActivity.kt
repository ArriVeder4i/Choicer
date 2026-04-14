package com.example.choicer

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.room.Room
import com.example.choicer.data.*
import com.example.choicer.uiu.MainApp
import com.example.choicer.viewmodel.MovieViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.nfc.tech.Ndef
import android.nfc.Tag

class MainActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var pendingIntent: PendingIntent

    private fun hideSystemBars() {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                1001
            )
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_MUTABLE
        )

        window.setWindowAnimations(0)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "movie-database"
        ).fallbackToDestructiveMigration().build()

        val dao = db.movieDao()

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()

        val clipRepository = ClipRepository(okHttpClient)

        val kinopoiskApi = Retrofit.Builder()
            .baseUrl("https://kinopoiskapiunofficial.tech/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KinopoiskApi::class.java)
        val movieCatalogRepository = MovieCatalogRepository(kinopoiskApi)

        setContent {
            val viewModel: MovieViewModel =
                androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return MovieViewModel(
                                movieCatalogRepository,
                                dao,
                                clipRepository
                            ) as T
                        }
                    }
                )

            MainApp(viewModel)
        }

        window.decorView.post {
            hideSystemBars()
        }
    }

    override fun onResume() {
        super.onResume()

        // 📥 ReaderMode (чтение)
        nfcAdapter?.enableReaderMode(
            this,
            { tag: Tag ->
                val ndef = Ndef.get(tag)
                ndef?.connect()

                val message = ndef?.ndefMessage
                val payload = message?.records?.firstOrNull()?.payload

                payload?.let {
                    val data = String(it)
                    android.util.Log.d("NFC", "DATA RECEIVED: $data")

                    runOnUiThread {
                        BleManager.onReceive(data)
                    }
                }

                ndef?.close()
            },
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B,
            null
        )

        // 📥 ForegroundDispatch (fallback)
        nfcAdapter?.enableForegroundDispatch(
            this,
            pendingIntent,
            null,
            null
        )
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableReaderMode(this)
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val rawMessages =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, NdefMessage::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                    ?.filterIsInstance<NdefMessage>()
                    ?.toTypedArray()
            }

        if (rawMessages != null) {
            val message = rawMessages[0] as NdefMessage
            val data = String(message.records[0].payload)
            BleManager.onReceive(data)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            hideSystemBars()
        }
    }
}
