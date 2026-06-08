package com.zzy.champions.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChampionAppViewModel @Inject constructor() : ViewModel() {
    var splashDone by mutableStateOf(false)
        private set

    fun onSplashFinished() {
        splashDone = true
    }
}
