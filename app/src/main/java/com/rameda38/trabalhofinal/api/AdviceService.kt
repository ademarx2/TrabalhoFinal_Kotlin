package com.rameda38.trabalhofinal.api

import retrofit2.Call
import retrofit2.http.GET

interface AdviceService {
    @GET("advice")
    fun getRandomAdvice(): Call<AdviceResponse>
}
