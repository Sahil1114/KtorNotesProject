package com.example.authentication

import com.example.data.checkPasswordForEmail
import io.ktor.auth.*

fun Authentication.Configuration.configureAuth(){
    basic {
        realm="Note Server"
        validate {credentials->
            val email=credentials.name
            val password=credentials.password
            if(checkPasswordForEmail(email,password)){
                UserIdPrincipal(email)
            }else null
        }
    }
}