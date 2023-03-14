package dev.cashlo.robohongpt

import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cashlo.robohongpt.api.ChatGptApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sbs.util.robohon.Behavior
import sbs.util.robohon.Emotion
import sbs.util.robohon.Robohon
import java.util.*

class RobohonViewModel: ViewModel()  {
    fun start(robohon: Robohon, recognizer: SpeechRecognizer) {
        viewModelScope.launch(Dispatchers.IO) {
            //robohon.speak(Locale.ENGLISH, "ChatGPT Start", Emotion.Happiness_2, Behavior.ID_0x06001d_自分を指す)
            //robohon.enableListening(Locale.ENGLISH)
            //robohon!!.disableListening()
        }
    }

    fun onRecognize(robohon: Robohon, speech: String, speechRecognizer: SpeechRecognizer) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = ChatGptApiClient.getResponse(speech)
            robohon.speak(response.text, Emotion.valueOf(response.emotion, response.level), Behavior.ID_0x06001d_自分を指す)
            robohon.waitForSpeechToFinish()

            val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "yue_Hant_HK")
            //          speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            CoroutineScope(Dispatchers.Main).launch {
                speechRecognizer.startListening(speechRecognizerIntent)
            }


        }
    }

}