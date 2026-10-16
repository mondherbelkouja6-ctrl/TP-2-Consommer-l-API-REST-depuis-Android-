package com.example.tp2.ui.consultations

import com.example.tp2.data.model.Consultation

// Les seuls états possibles de l'écran des consultations
sealed interface ConsultationsUiState {
    data object Loading : ConsultationsUiState
    data class Success(val consultations: List<Consultation>) : ConsultationsUiState
    data class Error(val message: String) : ConsultationsUiState
}
