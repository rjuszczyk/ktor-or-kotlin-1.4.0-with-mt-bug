package com.jetbrains.handson.mpp.mobile

import io.ktor.client.HttpClient
import io.ktor.client.features.logging.DEFAULT
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.get
import kotlinx.coroutines.*

expect fun platformName(): String

fun createApplicationScreenMessage(callback: (String) -> Unit) {

    val httpClient = HttpClient{
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
    }

    CoroutineScope(Dispatchers.Main).launch {
        val stationsResponse = httpClient.get<String>("https://staging.radoair.rjuszczyk.pl/stations")
        callback(stationsResponse)
    }
}