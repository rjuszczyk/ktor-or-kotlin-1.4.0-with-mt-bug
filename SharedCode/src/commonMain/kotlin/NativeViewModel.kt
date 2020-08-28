package com.jetbrains.handson.mpp.mobile

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.broadcast
import kotlinx.coroutines.flow.*

open class NativeViewModel {

    val coroutineScope = CoroutineScope(Dispatchers.Main)

    /**
     * This method should be called when ViewModel's hosting component is destroyed and no further interaction with ViewModel is possible.
     */
    fun dispose() {
        coroutineScope.cancel()
    }

    inner class MutableFlow<T>(initialValue: T? = null): Flow<T> {

        private var lastValue: T? = initialValue
        private val valueChannel = Channel<T>()

        @ExperimentalCoroutinesApi
        @FlowPreview
        private val valueFlow = coroutineScope.broadcast {
            for(value in valueChannel) {
                lastValue = value
                send(value)
            }
        }.asFlow().onStart {
            lastValue?.let { emit(it) }
        }

        @InternalCoroutinesApi
        @ExperimentalCoroutinesApi
        @FlowPreview
        override suspend fun collect(collector: FlowCollector<T>) {
            valueFlow.collect(collector)
        }

        fun postValue(value: T) {
            if (!valueChannel.offer(value)) {
                lastValue = value
            }
        }
    }
}

fun <F, T : NativeViewModel> T.toCompletion(block: T.() -> Flow<F>, completion: (F) -> (Unit)) {
    coroutineScope.launch { block(this@toCompletion).collect{ value -> completion(value) } }
}