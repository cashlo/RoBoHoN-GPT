package dev.cashlo.robohongpt

import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cashlo.robohongpt.api.ChatGptApiClient
import dev.cashlo.robohongpt.api.ChatGptPrompt
import dev.cashlo.robohongpt.api.NewsApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.osdn.util.ssdp.Device
import net.osdn.util.ssdp.client.SsdpClient
import sbs.util.robohon.Behavior
import sbs.util.robohon.Emotion
import sbs.util.robohon.Robohon
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

    private val newsOrder = listOf("top","science,technology","entertainment")

    fun startRadio(robohon: Robohon) {
        viewModelScope.launch(Dispatchers.IO) {
            var prompt = "Let's do a radio news show\"Robohon News\"! Here's the topics we will cover:\n" +
                    newsOrder.joinToString() + "\n" +
                    "Make sure to welcome listener to the show, and mention when you change topic:"
            //"ラジオ番組をやりましょう！こちらはニュースです。各ニュース項目に教えてください, ついてユーモアを交えながら議論してください。\n"

            newsOrder.forEach { category ->
                val newsResponse = NewsApiClient.getNews(null, "en", category)

                prompt += "here's the $category news items.\n" +
                        "for each news item, please give me a summary, and then do a discussion with humor and jokes.\n"
                var newsCount = 0
                newsResponse!!
                    .take(3)
                    .forEach {
                        Log.d("RobohonViewModel", "$category news title: ${it.title}")
                        prompt += it.title + "\n" +
                                it.description + "\n"
                        newsCount += 1
                        if (newsCount % 3 == 0 || newsCount == newsResponse.size) {
                            speakInPair(prompt, robohon, Locale.ENGLISH)
                            prompt = ""
                        }
                    }
            }
            speakInPair("Thank the listeners for joining the show", robohon, Locale.ENGLISH)

                //"ラジオ番組をやりましょう！こちらはニュースです。各ニュース項目に教えてください, ついてユーモアを交えながら議論してください。\n"


        }
    }

    fun onRecognize(
        robohon: Robohon,
        speech: String,
        speechRecognizer: SpeechRecognizer
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            speakInPair(speech, robohon, Locale.JAPANESE)

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

    private fun speakInPair(prompt: String, robohon: Robohon, locale: Locale) {
        val response = ChatGptApiClient.getResponse(prompt, ChatGptPrompt.Prompt.EN_PAIR)
        response!!.forEach {
            if (it.name == "R2") {
                Log.d("RobohonViewModel", "R2: ${it.text}")
                if (::remoteRobohon.isInitialized) {
                    remoteRobohon.speak(
                        locale,
                        it.text,
                        sbs.util.robohon.remote.Emotion.valueOf(it.emotion, it.level),
                        null,
                        // sbs.util.robohon.remote.Behavior.ID_0x06001d_自分を指す
                    )
                }
                remoteRobohon.waitForSpeechToFinish()
            } else {
                Log.d("RobohonViewModel", "R1: ${it.text}")
                robohon.speak(
                    locale,
                    it.text,
                    Emotion.valueOf(it.emotion, it.level),
                    null,
                    // Behavior.ID_0x06001d_自分を指す
                )
            }
            robohon.waitForSpeechToFinish()
        }
    }

}