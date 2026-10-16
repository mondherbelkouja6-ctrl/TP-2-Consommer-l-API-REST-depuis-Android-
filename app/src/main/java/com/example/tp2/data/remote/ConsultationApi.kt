package com.example.tp2.data.remote

import com.example.tp2.data.model.Consultation
import retrofit2.http.GET
import retrofit2.http.Query

interface ConsultationApi {

    // -> GET /api/consultations?patient=<id>
    @GET("consultations")
    suspend fun getConsultations(@Query("patient") patientId: String): List<Consultation>
}