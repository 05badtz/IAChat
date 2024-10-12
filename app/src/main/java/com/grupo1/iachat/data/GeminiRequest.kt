package com.grupo1.iachat.data

data class RequestBody(
    val contents: List<ContentBody>
)

data class ContentBody(
    val parts: List<PartBody>
)

data class PartBody(
    val text: String
)