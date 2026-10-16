package com.example.tp2.data.model

import kotlinx.serialization.Serializable

// Consultation telle que renvoyée par l'API (GET /api/consultations?patient=<id>)
@Serializable
data class Consultation(
    val id: String,
    val patient: PatientRef? = null,   // objet (populate), null si le patient a été supprimé
    val date: String,                  // date ISO, ex. "2026-10-16T09:00:00.000Z"
    val motif: String,
    val traitement: String? = null,
    val medecin: String,
    val notes: String? = null,
    val statut: String = "En attente",
)

// Patient réduit renvoyé par populate("patient", "nom prenom")
@Serializable
data class PatientRef(
    val id: String? = null,
    val nom: String? = null,
    val prenom: String? = null,
)