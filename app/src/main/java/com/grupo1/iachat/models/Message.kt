package com.grupo1.iachat.models

import com.grupo1.iachat.data.PartBody

class Message (
    val itsMine:Boolean,
    val partBody: PartBody,
    val createdAt: String
)
