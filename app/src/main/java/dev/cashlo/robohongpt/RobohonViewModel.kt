package dev.cashlo.robohongpt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cashlo.robohongpt.api.ChatGptApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sbs.util.robohon.Behavior
import sbs.util.robohon.Emotion
import sbs.util.robohon.Robohon

class RobohonViewModel: ViewModel()  {
    fun start(robohon: Robohon) {
        viewModelScope.launch(Dispatchers.IO) {
            robohon.speak("ChatGPT Start", Emotion.Happiness_2, Behavior.ID_0x06001d_自分を指す)
            robohon.enableListening()
        }
    }

    fun onRecognize(robohon: Robohon, speech: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = ChatGptApiClient.getResponse(speech)
            robohon.speak(response.text, Emotion.valueOf(response.emotion, response.level), Behavior.ID_0x06001d_自分を指す)
        }
    }

}