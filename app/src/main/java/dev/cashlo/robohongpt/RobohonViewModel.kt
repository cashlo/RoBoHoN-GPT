package dev.cashlo.robohongpt

import android.content.Intent
import android.os.Build
import android.os.Environment
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.annotation.RequiresApi
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
import sbs.util.robohon.Emotion
import sbs.util.robohon.Robohon
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.util.*


class RobohonViewModel : ViewModel() {
    private val remoteRobohon = mutableListOf<sbs.util.robohon.remote.Robohon>()

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
                    remoteRobohon.add(sbs.util.robohon.remote.Robohon(device.getAddress()))
                }
            }
        }
    }

    private val newsOrder = listOf("top","science,technology","entertainment")

    fun startRadio(robohon: Robohon) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = LocalDateTime.now().toString()
            var prompt = "The time is now $current Let's do a radio news show\"Robohon News\"! Here's the topics we will cover:\n" +
                    newsOrder.joinToString() + "\n" +
                    "There will be around 3 news items for each topics. Make sure to mention the time now and welcome listener to the show:"

            val speechList = mutableListOf<ChatGptApiClient.Speech>()
            newsOrder.forEach { category ->
                val newsResponse = NewsApiClient.getNews(null, "en", category, "bbc,google")

                prompt += "here's the $category news items. Please remember to response in a single JSON\n" +
                        "for each news item, please give me a summary, and then do a in-depth discussion with humor and jokes, take on roles of different perspectives around the news items.\n"
                var newsCount = 0
                newsResponse!!
                    .take(3)
                    .forEach {
                        Log.d("RobohonViewModel", "$category news title: ${it.title}")
                        prompt += "topic: $category news\n" + it.title + "\n" +
                                it.description + "\n"
                        newsCount += 1
                        if (newsCount % 3 == 0 || newsCount == newsResponse.size) {
                            val responseSpeechList = ChatGptApiClient
                                .getResponse(prompt, ChatGptPrompt.Prompt.EN_PAIR, remoteRobohon.size + 1)
                            speechList.addAll(responseSpeechList!!)
                            prompt = ""
                            robohon.speak(
                                Locale.ENGLISH,
                                "$category news ready",
                                null,
                                null
                            )
                        }
                    }
            }
            val goodbyeSpeechList = ChatGptApiClient
                .getResponse("Thank the listeners for joining the show", ChatGptPrompt.Prompt.EN_PAIR, remoteRobohon.size + 1)
            speechList.addAll(goodbyeSpeechList!!)
            robohon.speak(
                Locale.ENGLISH,
                "Robohon news show ready, starting in 3, 2",
                null,
                null
            )
            Thread.sleep(1000)
            speakAll(speechList, robohon, Locale.ENGLISH)

                //"ラジオ番組をやりましょう！こちらはニュースです。各ニュース項目に教えてください, ついてユーモアを交えながら議論してください。\n"
        }
    }

    fun startRadioJp(robohon: Robohon) {
        viewModelScope.launch(Dispatchers.IO) {
            var prompt =  "ラジオニュースショー\"ロボホンニュース\"をやりましょう！こちらはトピックスです:\n" +
                    newsOrder.joinToString() + "\n" +
                    "各トピックに対応したセグメントがあります。トピックごとに約 3 つのニュース項目があります、リスナーを歓迎し:"
            //"ラジオ番組をやりましょう！こちらはニュースです。各ニュース項目に教えてください, ついてユーモアを交えながら議論してください。\n"

            val speechList = mutableListOf<ChatGptApiClient.Speech>()
            newsOrder.forEach { category ->
                val newsResponse = NewsApiClient.getNews(null, "jp", category, "yahoo_jp,google,nhk")

                prompt += "ここに、$category セグメントのすべてのニュースがあります。単一の JSON で応答することを忘れないでください\n" +
                        "各ニュース項目について、要約をして、詳しい話をして、ユーモアとジョークを交えながら、ニュース項目に関わる様々な視点を取り入れてください。\n"
                var newsCount = 0
                newsResponse!!
                    .take(5)
                    .forEach {
                        Log.d("RobohonViewModel", "$category news title: ${it.title}")
                        prompt += it.title + "\n" +
                                it.description + "\n"
                        newsCount += 1
                        if (newsCount % 5 == 0 || newsCount == newsResponse.size) {
                            val responseSpeechList = ChatGptApiClient
                                .getResponse(prompt, ChatGptPrompt.Prompt.JP_PAIR, remoteRobohon.size + 1)
                            speechList.addAll(responseSpeechList!!)
                            prompt = ""
                        }
                    }
            }
            val goodbyeSpeechList = ChatGptApiClient
                .getResponse("番組を聴いてくれたリスナーに感謝を示して", ChatGptPrompt.Prompt.JP_PAIR, remoteRobohon.size + 1)
            speechList.addAll(goodbyeSpeechList!!)
            speakAll(speechList, robohon, Locale.JAPANESE)


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

    private fun speakAll(speechList: List<ChatGptApiClient.Speech>, robohon: Robohon, locale: Locale) {
        speechList!!.forEach {
            if (it.name == "R1") {
                Log.d("RobohonViewModel", "R1: ${it.text}")
                robohon.speak(
                    locale,
                    it.text,
                    Emotion.valueOf(it.emotion, it.level),
                    null,
                    // Behavior.ID_0x06001d_自分を指す
                )
            } else {
                Log.d("RobohonViewModel", "${it.name}: ${it.text}")
                val robohonIndex = it.name!![1].digitToInt() - 2
                if (remoteRobohon.size > robohonIndex) {
                    remoteRobohon[robohonIndex].speak(
                        locale,
                        it.text,
                        sbs.util.robohon.remote.Emotion.valueOf(it.emotion, it.level),
                        null,
                        // sbs.util.robohon.remote.Behavior.ID_0x06001d_自分を指す
                    )
                    remoteRobohon[robohonIndex].waitForSpeechToFinish()
                }
            }
            robohon.waitForSpeechToFinish()
        }
    }
    private fun speakInPair(prompt: String, robohon: Robohon, locale: Locale) {
        var promptType = ChatGptPrompt.Prompt.EN_PAIR
        if (locale == Locale.JAPANESE) {
            promptType = ChatGptPrompt.Prompt.JP_PAIR
        }
        val response = ChatGptApiClient.getResponse(prompt, promptType, remoteRobohon.size + 1)
        response!!.forEach {
            if (it.name == "R1") {
                Log.d("RobohonViewModel", "R1: ${it.text}")
                robohon.speak(
                    locale,
                    it.text,
                    Emotion.valueOf(it.emotion, it.level),
                    null,
                    // Behavior.ID_0x06001d_自分を指す
                )
            } else {
                Log.d("RobohonViewModel", "${it.name}: ${it.text}")
                val robohonIndex = it.name!![1].digitToInt() - 2
                if (remoteRobohon.size > robohonIndex) {
                    remoteRobohon[robohonIndex].speak(
                        locale,
                        it.text,
                        sbs.util.robohon.remote.Emotion.valueOf(it.emotion, it.level),
                        null,
                        // sbs.util.robohon.remote.Behavior.ID_0x06001d_自分を指す
                    )
                    remoteRobohon[robohonIndex].waitForSpeechToFinish()
                }
            }
            robohon.waitForSpeechToFinish()
        }
    }

}