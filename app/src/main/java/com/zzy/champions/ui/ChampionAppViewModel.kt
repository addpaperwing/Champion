package com.zzy.champions.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChampionAppViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    var splashDone by mutableStateOf(savedStateHandle.get<Boolean>(KEY_SPLASH_DONE) == true)
        private set

    fun onSplashFinished() {
        splashDone = true
        savedStateHandle[KEY_SPLASH_DONE] = true
    }

    private companion object {
        const val KEY_SPLASH_DONE = "splash_done"
    }
}
