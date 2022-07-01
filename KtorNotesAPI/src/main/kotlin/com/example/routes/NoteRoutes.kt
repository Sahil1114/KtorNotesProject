package com.example.routes

import com.example.data.*
import com.example.data.collections.Note
import com.example.data.requests.AddOwnerRequest
import com.example.data.requests.DeleteNoteRequest
import com.example.data.response.SimpleResponse
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlin.text.get

fun Route.noteRoutes(){
    route("/getNotes") {
        authenticate {
            get{
                val email = call.principal<UserIdPrincipal>()!!.name
                val notes = getNotesForUsers(email)
                call.respond(OK, notes)
            }
        }
    }
    route("/deleteNote") {
        authenticate {
            post {
                val email = call.principal<UserIdPrincipal>()!!.name
                val request = try {
                    call.receive<DeleteNoteRequest>().id
                } catch(e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@post
                }
                println(request)
                if(deleteNoteForUser(email, request )) {
                    call.respond(OK)
                } else {
                    call.respond(Conflict)
                }
            }
        }
    }
    route("/addNote"){
        authenticate {
            post {
                println()
                val note=try {
                    call.receive<Note>()
                }catch (e:ContentTransformationException){
                    call.respond(BadRequest)
                    return@post
                }

                if(saveNote(note)){
                    call.respond(OK)
                }else{
                    call.respond(Conflict)
                }
            }
        }
    }
    route("/addOwnerToNote"){
        authenticate {
            post {
                val request=try {
                    call.receive<AddOwnerRequest>()
                }catch (e:ContentTransformationException){
                    call.respond(BadRequest)
                    return@post
                }
                if(!checkIfUsersExists(request.owner)){
                    call.respond(
                        OK,
                        SimpleResponse(
                            false,
                            "No user with email exist"
                        )
                    )
                    return@post
                }
                if(isOwnerOfNote(request.noteId,request.owner)){
                    call.respond(
                        OK,
                        SimpleResponse(
                            false,
                            "This user is already an owner of this note"
                        )
                    )
                    return@post
                }
                if(addOwnersToNote(request.noteId,request.owner)){
                    call.respond(
                        OK,
                        SimpleResponse(
                            true,
                            "${request.owner} can now see this note"
                        )
                    )
                }else{
                    call.respond(Conflict)
                }
            }
        }
    }

}