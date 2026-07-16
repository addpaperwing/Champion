package com.zzy.champions.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

const val DEFAULT_STATE_STOP_TIMEOUT_MS = 5_000L

fun <T> Flow<T>.stateInViewModel(
    scope: CoroutineScope,
    initialValue: T,
    started: SharingStarted = SharingStarted.WhileSubscribed(DEFAULT_STATE_STOP_TIMEOUT_MS),
): StateFlow<T> = stateIn(scope, started, initialValue)
