package dev.cashlo.robohongpt

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import sbs.util.robohon.CancelReason
import sbs.util.robohon.Robohon
import sbs.util.robohon.RobohonCallback
import sbs.util.robohon.VoiceRecognition


class MainActivity : AppCompatActivity() {
    private var robohon: Robohon? = null
    private var callback: RobohonCallback? = null
    private val robohonViewModel = RobohonViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        robohon = Robohon(this)

        callback = object : RobohonCallback {
            override fun onVoiceUIServiceStarted() {}
            override fun onSpeakComplete() {}
            override fun onCancel(reason: CancelReason) {
                Log.d("RobohonCallback", "onCancel $reason")
            }
            override fun onReject() {
                Log.d("RobohonCallback", "onReject")
            }
            override fun onRecognize(recognition: VoiceRecognition) {
                Log.d("RobohonCallback", "onRecognize")
                robohonViewModel.onRecognize(robohon!!, recognition.basic)
            }
        }

        robohon!!.setCallback(callback)
        robohonViewModel.start(robohon!!)
    }


    override fun onPause() {
        if (robohon != null) {
            robohon!!.release()
            robohon = null
        }
        super.onPause()
    }

}

