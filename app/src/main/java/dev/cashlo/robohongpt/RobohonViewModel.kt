package dev.cashlo.robohongpt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sbs.util.robohon.Robohon

class RobohonViewModel: ViewModel()  {
    fun start(robohon: Robohon) {
        // Create a new coroutine to move the execution off the UI thread
        viewModelScope.launch(Dispatchers.IO) {
            robohon!!.speak("ChatGPT Start")
            robohon!!.enableListening()
        }
    }
}