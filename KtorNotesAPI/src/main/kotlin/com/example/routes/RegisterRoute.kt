package com.example.routes

import com.example.data.checkIfUsersExists
import com.example.data.collections.User
import com.example.data.registerUser
import com.example.data.requests.AccountRequests
import com.example.data.response.SimpleResponse
import com.example.security.getHashWithSalt
import io.ktor.application.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.registerRoute(){
    route("/register"){
        post {
            val request=try {
                call.receive<AccountRequests>()
            }catch (e:ContentTransformationException){
                call.respond(BadRequest)
                return@post
            }
            val userExist= checkIfUsersExists(request.email)
            if(!userExist){
                if(registerUser(User(request.email, getHashWithSalt(request.password)))){
                    call.respond(OK, SimpleResponse(true,"Successfully created account"))
                }else{
                    call.respond(OK,SimpleResponse(false,"An unknown error occurred"))
                }
            }else{
                call.respond(OK,SimpleResponse(false,"A user with that E-Mail already exits"))
            }

        }
    }
}