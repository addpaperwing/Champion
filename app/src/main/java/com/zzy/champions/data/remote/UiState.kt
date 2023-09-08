package com.zzy.champions.data.remote

sealed class UiState<out T> {

    object Idle: UiState<Nothing>()
    object Loading : UiState<Nothing>()

    data class Error(val exception: Throwable) : UiState<Nothing>() {

        fun getErrorMessage(): String? {
            return exception.message
        }
    }

    data class Success<out T>(val data: T) : UiState<T>()
}