package com.izhenius.aigent.presentation.mvi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class MviViewModel<UiState : Any, UiAction : Any> : ViewModel() {

    private val mutableUiStateFlow: MutableStateFlow<UiState> = MutableStateFlow(setInitialUiState())
    val uiStateFlow: StateFlow<UiState> = mutableUiStateFlow.asStateFlow()
    val uiState: UiState get() = uiStateFlow.value

    abstract fun setInitialUiState(): UiState

    abstract fun onUiAction(uiAction: UiAction)

    protected fun updateUiState(uiStateAction: UiState.() -> UiState) {
        mutableUiStateFlow.update { it.uiStateAction() }
    }
}