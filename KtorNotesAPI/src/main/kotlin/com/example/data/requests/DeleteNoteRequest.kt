package com.example.data.requests

import org.bson.codecs.pojo.annotations.BsonId

data class DeleteNoteRequest(
    @BsonId
    val id: String
)
