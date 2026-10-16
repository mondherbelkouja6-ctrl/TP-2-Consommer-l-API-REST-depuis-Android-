package com.example.tp2.ui.consultations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tp2.PatientsApplication
import com.example.tp2.data.repository.ConsultationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConsultationsViewModel(
    private val repository: ConsultationRepository,
    private val patientId: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ConsultationsUiState>(ConsultationsUiState.Loading)
    val uiState: StateFlow<ConsultationsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = ConsultationsUiState.Loading
        viewModelScope.launch {
            repository.getConsultations(patientId)
                .onSuccess { _uiState.value = ConsultationsUiState.Success(it) }
                .onFailure { e -> _uiState.value = ConsultationsUiState.Error(e.message ?: "Erreur inconnue") }
        }
    }

    companion object {
        // Ce ViewModel a besoin de l'id du patient : la Factory en est une fonction
        fun factory(patientId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as PatientsApplication
                ConsultationsViewModel(app.container.consultationRepository, patientId)
            }
        }
    }
}
