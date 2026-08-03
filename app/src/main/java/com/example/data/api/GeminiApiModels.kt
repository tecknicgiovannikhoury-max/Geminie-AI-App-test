package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<ContentRequest>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: ContentRequest? = null
)

@JsonClass(generateAdapter = true)
data class ContentRequest(
    @Json(name = "parts") val parts: List<PartRequest>,
    @Json(name = "role") val role: String? = null
)

@JsonClass(generateAdapter = true)
data class PartRequest(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inlineData") val inlineData: InlineDataRequest? = null
)

@JsonClass(generateAdapter = true)
data class InlineDataRequest(
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "data") val data: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "temperature") val temperature: Float? = 0.7f,
    @Json(name = "topP") val topP: Float? = 0.95f,
    @Json(name = "topK") val topK: Int? = 40
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<CandidateResponse>? = null
)

@JsonClass(generateAdapter = true)
data class CandidateResponse(
    @Json(name = "content") val content: ContentResponse? = null
)

@JsonClass(generateAdapter = true)
data class ContentResponse(
    @Json(name = "parts") val parts: List<PartResponse>? = null
)

@JsonClass(generateAdapter = true)
data class PartResponse(
    @Json(name = "text") val text: String? = null
)
