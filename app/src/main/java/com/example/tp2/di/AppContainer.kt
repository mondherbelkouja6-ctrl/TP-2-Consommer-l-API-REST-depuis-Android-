package com.example.tp2.di

import com.example.tp2.data.remote.ConsultationApi
import com.example.tp2.data.remote.PatientApi
import com.example.tp2.data.repository.ConsultationRepository
import com.example.tp2.data.repository.PatientRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

// Émulateur : 10.0.2.2 — Téléphone réel : IP du PC
private const val BASE_URL = "http://192.168.1.106:5000/api/"
class AppContainer {

    // Ignore les champs JSON absents de nos classes (createdAt, updatedAt...)
    private val json = Json { ignoreUnknownKeys = true }

    // Affiche chaque requête et réponse dans Logcat (filtre : okhttp)
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            },
        )
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val patientApi: PatientApi = retrofit.create(PatientApi::class.java)
    private val consultationApi: ConsultationApi = retrofit.create(ConsultationApi::class.java)

    val patientRepository = PatientRepository(patientApi, json)
    val consultationRepository = ConsultationRepository(consultationApi, json)
}