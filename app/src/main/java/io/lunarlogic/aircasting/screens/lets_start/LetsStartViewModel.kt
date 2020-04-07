package io.lunarlogic.aircasting.screens.lets_start

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LetsStartViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is let start Fragment"
    }
    val text: LiveData<String> = _text
}