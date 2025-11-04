package com.izhenius.aigent.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.cancellation.CancellationException

fun ViewModel.launch(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    onError: (Throwable) -> Unit = {},
    finallyBlock: () -> Unit = {},
    block: suspend CoroutineScope.() -> Unit,
): Job = viewModelScope.launch(context, start) {
    try {
        block(this)
    } catch (e: CancellationException) {
        throw e
    } catch (throwable: Throwable) {
        onError(throwable)
    } finally {
        finallyBlock()
    }
}

fun <T> ViewModel.async(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    onError: (Throwable) -> Unit = {},
    finallyBlock: () -> Unit = {},
    block: suspend CoroutineScope.() -> T,
): Deferred<T?> = viewModelScope.async(context, start) {
    try {
        block(this)
    } catch (e: CancellationException) {
        throw e
    } catch (throwable: Throwable) {
        onError(throwable)
        null
    } finally {
        finallyBlock()
    }
}
