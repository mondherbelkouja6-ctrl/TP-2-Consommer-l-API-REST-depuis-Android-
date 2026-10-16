package com.example.tp2.ui.consultations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tp2.data.model.Consultation
import com.example.tp2.data.model.Patient
import com.example.tp2.ui.patients.ErrorContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultationsScreen(
    patient: Patient,
    onBack: () -> Unit,
    // key = patient.id : un ViewModel différent pour chaque patient
    viewModel: ConsultationsViewModel = viewModel(
        key = patient.id,
        factory = ConsultationsViewModel.factory(patient.id),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consultations – ${patient.prenom} ${patient.nom}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (val state = uiState) {
                is ConsultationsUiState.Loading ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))

                is ConsultationsUiState.Error ->
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.load() },
                        modifier = Modifier.align(Alignment.Center),
                    )

                is ConsultationsUiState.Success ->
                    ConsultationList(state.consultations)
            }
        }
    }
}

@Composable
fun ConsultationList(consultations: List<Consultation>) {
    if (consultations.isEmpty()) {
        Text("Aucune consultation", modifier = Modifier.padding(24.dp))
    } else {
        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(consultations, key = { it.id }) { consultation ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = consultation.motif ?: "Consultation",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = listOfNotNull(consultation.date, consultation.notes)
                                .joinToString(" · "),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}
