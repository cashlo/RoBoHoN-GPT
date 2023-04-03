package dev.cashlo.robohongpt

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import sbs.util.robohon.Robohon
import java.net.InetAddress


class MainActivity : AppCompatActivity() {
    private lateinit var robohon: Robohon
    private val robohonViewModel = RobohonViewModel()
    private val recordAudioRequestCode = 1
    private lateinit var startButton: Button
    private lateinit var speechRecognizer: SpeechRecognizer

    fun startListening() {
        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "yue_Hant_HK")
        //speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en_HK")
        //          speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        speechRecognizer!!.startListening(speechRecognizerIntent)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startButton = findViewById(R.id.startButton)
        startButton!!.setOnClickListener() {
            startListening()
        }

        //ホームボタンの検知登録.
        val filterHome = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(contxt: Context?, intent: Intent?) {
                    Log.d("onResults", "Receive Home button pressed")
            }
        }, filterHome)
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                recordAudioRequestCode
            )
        }
    }

    override fun onResume() {
        super.onResume()
        robohon = Robohon(this)
        robohonViewModel.start()

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        speechRecognizer!!.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(bundle: Bundle) {}
            override fun onBeginningOfSpeech() {
                Log.d("MainActivity", "Listening...")
            }

            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(bytes: ByteArray) {}
            override fun onEndOfSpeech() {}
            override fun onError(i: Int) {}
            override fun onResults(bundle: Bundle) {
                val data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                Log.d("onResults", data!![0])
                robohonViewModel.onRecognize(robohon, data[0], speechRecognizer!!)
            }

            override fun onPartialResults(bundle: Bundle) {}
            override fun onEvent(i: Int, bundle: Bundle) {}
        })
        startListening()
        robohonViewModel.startRadio(robohon)
    }


    override fun onPause() {
        robohon.release()
        super.onPause()
    }

}

