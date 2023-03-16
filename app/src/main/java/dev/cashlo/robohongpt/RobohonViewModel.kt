package dev.cashlo.robohongpt

import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cashlo.robohongpt.api.ChatGptApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.osdn.util.ssdp.Device
import net.osdn.util.ssdp.client.SsdpClient
import sbs.util.robohon.Behavior
import sbs.util.robohon.Emotion
import sbs.util.robohon.Robohon
import java.net.InetAddress
import java.util.*


class RobohonViewModel : ViewModel() {
    private lateinit  var remoteRobohon: sbs.util.robohon.remote.Robohon

    fun start() {
        viewModelScope.launch(Dispatchers.IO) {
            //robohon.speak(Locale.ENGLISH, "ChatGPT Start", Emotion.Happiness_2, Behavior.ID_0x06001d_自分を指す)
            //robohon.enableListening(Locale.ENGLISH)
            //robohon!!.disableListening()
            val client = SsdpClient()
            client.discover()
            val devices: List<Device> = client.get() //このメソッド呼び出しは15秒間復帰しません。

            for (device: Device in devices) {
                Log.d("start()",
                    "FriendlyName = " + device.getFriendlyName().toString() + "\n" +
                            "Manufacturer = " + device.getManufacturer().toString() + "\n" +
                            "ModelName = " + device.getModelName().toString() + "\n" +
                            "Address = " + device.getAddress().toString() + "\n" +
                            "ModelDescription = " + device.getModelDescription().toString() + "\n"
                )
                if (device.modelName == "SR01MW"){
                    remoteRobohon = sbs.util.robohon.remote.Robohon(device.getAddress())
                }

            }

        }
    }

    fun onRecognize(
        robohon: Robohon,
        speech: String,
        speechRecognizer: SpeechRecognizer
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = ChatGptApiClient.getResponse(speech, ChatGptApiClient.Prompt.JP_PAIR)
            response!!.forEach {
                if (it.name == "R2") {
                    if (::remoteRobohon.isInitialized) {
                        remoteRobohon.speak(
                            it.text,
                            sbs.util.robohon.remote.Emotion.valueOf(it.emotion, it.level),
                            sbs.util.robohon.remote.Behavior.ID_0x06001d_自分を指す
                        )
                    }
                    remoteRobohon.waitForSpeechToFinish()
                } else {
                    robohon.speak(
                        it.text,
                        Emotion.valueOf(it.emotion, it.level),
                        Behavior.ID_0x06001d_自分を指す
                    )
                }
                robohon.waitForSpeechToFinish()
            }


            val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            speechRecognizerIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "yue_Hant_HK")
            //speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en_HK")
            //          speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            CoroutineScope(Dispatchers.Main).launch {
                speechRecognizer.startListening(speechRecognizerIntent)
            }


        }
    }

}