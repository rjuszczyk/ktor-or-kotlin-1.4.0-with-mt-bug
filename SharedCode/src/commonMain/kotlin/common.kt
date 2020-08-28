package com.jetbrains.handson.mpp.mobile

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.*
//import io.ktor.utils.io.printStack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

expect fun platformName(): String

fun createApplicationScreenMessage(callback: (String) -> Unit) {

//    val httpClient = HttpClient{}
//    CoroutineScope(Dispatchers.Main).launch {
//        try {
//            val stationsResponse =
//                httpClient.get<String>("https://staging.radoair.rjuszczyk.pl/stations")
//            callback("loaded: $stationsResponse")
//        } catch (t: Throwable) {
//            callback(t.message?:"error")
//        }
//
//    }


    val myNativeViewModel = MyNativeViewModel()
    myNativeViewModel.getState(callback)

    myNativeViewModel.doSomething()
}

class MyNativeViewModel: NativeViewModel() {
    val httpClient = HttpClient{}
//    private val state = MutableStateFlow("")
    private val state = MutableFlow("Kotlin Rocks on ${platformName()}")

    fun getState(completion: (String) -> (Unit)) =
        this.toCompletion({ state }, completion)

    fun doSomething() {

        coroutineScope.launch {

            delay(1500)
//            state.value = "loading"
            state.postValue("loading")
            delay(1500)
            try {
                val stationsResponse =
                    httpClient.get<String>("https://staging.radoair.rjuszczyk.pl/stations")
//                state.value = "loaded = $stationsResponse"
                state.postValue("loaded = $stationsResponse")
            } catch (e: Throwable) {
                println("failed = ${e.message}")
//                state.value = "failed = ${e.message}"
                state.postValue("failed = ${e.message}")
            }

        }
    }
}
