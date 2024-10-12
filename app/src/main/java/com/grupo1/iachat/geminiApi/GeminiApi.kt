package com.grupo1.iachat.geminiApi

import com.grupo1.iachat.data.RequestBody
import com.grupo1.iachat.data.ResponseData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query


// La interfaz es una definición abstracta de como se deberia interactuar con la API.
// Busca ser un contrato que especifica endpoints de una API y tipos de verbos HTTP
// que se deberian utilizar. Además se definen parametros, cuerpos de petición y headers
interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("/v1beta/models/gemini-1.5-flash-latest:generateContent")
    suspend fun askToGemini(
        @Query("key") apiKey: String,
        @Body requestBody: RequestBody
    ): Response<ResponseData>
}