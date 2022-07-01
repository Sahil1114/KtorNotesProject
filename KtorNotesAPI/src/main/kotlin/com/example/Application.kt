package com.example

import com.example.authentication.configureAuth
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.plugins.*
import com.example.routes.loginRoute
import com.example.routes.noteRoutes
import com.example.routes.registerRoute
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.routing.*

fun main() {
    embeddedServer(Netty, port = 8030, host = "0.0.0.0") {
        install(DefaultHeaders)
        install(CallLogging)
        install(ContentNegotiation) {
            gson {
                setPrettyPrinting()
            }
        }
        install(Authentication) {
            configureAuth()
        }
        install(Routing) {
            noteRoutes()
            registerRoute()
            loginRoute()
        }

        configureRouting()
    }.start(wait = true)
}
