package com.grupo1.iachat.data

data class ResponseData(
    val candidates: List<ResponseCandidate>,
    val usageMetadata: ResponseUsageMetadata
)

data class ResponseCandidate(
    val content: ResponseContent,
    val finishReason: String,
    val index: Int,
    val safetyRatings: List<ResponseSafetyRating>
)

data class ResponseContent(
    val parts: List<ResponsePart>,
    val role: String
)

data class ResponsePart(
    val text: String
)

data class ResponseSafetyRating(
    val category: String,
    val probability: String
)

data class ResponseUsageMetadata(
    val promptTokenCount: Int,
    val candidatesTokenCount: Int,
    val totalTokenCount: Int
)