package dev.cashlo.robohongpt

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import dev.cashlo.robohongpt.api.ChatGptApiClient
import dev.cashlo.robohongpt.voiceui.ScenarioDefinitions
import dev.cashlo.robohongpt.voiceui.VoiceUIListenerImpl
import dev.cashlo.robohongpt.voiceui.VoiceUIManagerUtil
import dev.cashlo.robohongpt.voiceui.VoiceUIVariableUtil
import jp.co.sharp.android.voiceui.VoiceUIManager
import jp.co.sharp.android.voiceui.VoiceUIVariable
import sbs.util.robohon.CancelReason
import sbs.util.robohon.Robohon
import sbs.util.robohon.RobohonCallback
import sbs.util.robohon.VoiceRecognition
import java.util.*


class MainActivity : AppCompatActivity(), VoiceUIListenerImpl.ScenarioCallback {
    private var robohon: Robohon? = null
    private var callback: RobohonCallback? = null
    private var mVUIManager: VoiceUIManager? = null
    private var mVUIListener: VoiceUIListenerImpl? = null
    private var mHomeEventReceiver: HomeEventReceiver? =
        null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //ホームボタンの検知登録.
        mHomeEventReceiver = HomeEventReceiver()
        val filterHome = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        registerReceiver(mHomeEventReceiver, filterHome)
    }
    override fun onResume() {
        super.onResume()
        //robohon = Robohon(this)

        callback = object : RobohonCallback {
            override fun onVoiceUIServiceStarted() {}
            override fun onSpeakComplete() {}
            override fun onCancel(reason: CancelReason) {}
            override fun onReject() {}
            override fun onRecognize(recognition: VoiceRecognition) {
                val response = ChatGptApiClient.getResponse(recognition.basic)
                robohon!!.speak(response)
            }
        }

        //RobohonViewModel().start(robohon!!)
        //return

        //VoiceUIManagerインスタンス生成.
        if (mVUIManager == null) {
            mVUIManager = VoiceUIManager.getService(applicationContext)
        }

        //VoiceUIListenerインスタンス生成.
        if (mVUIListener == null) {
            mVUIListener = VoiceUIListenerImpl(this)
        }

        //VoiceUIListenerの登録.
        VoiceUIManagerUtil.registerVoiceUIListener(mVUIManager, mVUIListener)

        //Scene有効化.
        VoiceUIManagerUtil.enableScene(mVUIManager, ScenarioDefinitions.SCENE_COMMON)

        //VoiceUIManagerUtil.setAsr(mVUIManager, Locale.JAPAN)
        //VoiceUIManagerUtil.setTts(mVUIManager, Locale.JAPAN)

        mVUIManager!!.notifyEnableMic()
    }

    override fun onScenarioEvent(event: Int, variables: MutableList<VoiceUIVariable>?) {
        when (event) {
            VoiceUIListenerImpl.ACTION_END -> {
                val function: String = VoiceUIVariableUtil.getVariableData(
                    variables,
                    ScenarioDefinitions.ATTR_FUNCTION
                )
                if (ScenarioDefinitions.FUNC_RECOG_TALK.equals(function)) {
                    val lvcsr: String = VoiceUIVariableUtil.getVariableData(
                        variables,
                        ScenarioDefinitions.KEY_LVCSR_BASIC
                    )
                    val response = ChatGptApiClient.getResponse(lvcsr)
                    robohon!!.speak(response)
                }
            }
            else -> {}
        }
    }

    private class HomeEventReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.v(MainActivity::class.java.simpleName, "Receive Home button pressed")
            // ホームボタン押下でアプリ終了する.
        }
    }
}