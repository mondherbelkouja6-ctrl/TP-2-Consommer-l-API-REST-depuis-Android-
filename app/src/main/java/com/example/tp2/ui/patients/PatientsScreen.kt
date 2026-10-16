package com.example.tp2.ui.patients

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tp2.data.model.Patient
import com.example.tp2.data.model.PatientRequest
import com.example.tp2.ui.consultations.ConsultationsScreen

// Exercice 3 : point d'entrée (même nom qu'avant, MainActivity ne change pas).
// Affiche soit la liste des patients, soit les consultations d'un patient.
@Composable
fun PatientsScreen() {
    var consultationPatient by remember { mutableStateOf<Patient?>(null) }
    val selected = consultationPatient

    if (selected != null) {
        BackHandler { consultationPatient = null }
        ConsultationsScreen(patient = selected, onBack = { consultationPatient = null })
    } else {
        PatientsListScreen(onShowConsultations = { consultationPatient = it })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientsListScreen(
    onShowConsultations: (Patient) -> Unit,
    viewModel: PatientViewModel = viewModel(factory = PatientViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val statutFilter by viewModel.statutFilter.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    // Exercice 1 : patient en cours de modification (null = aucun)
    var patientToEdit by remember { mutableStateOf<Patient?>(null) }

    // Affiche chaque message envoyé par le ViewModel
    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patients") },
                actions = {
                    IconButton(onClick = { viewModel.loadPatients() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualiser")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Exercice 2 : filtres au-dessus de la liste
            StatutFilterRow(
                selected = statutFilter,
                onSelect = { viewModel.setFilter(it) },
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (val state = uiState) {
                    is PatientsUiState.Loading ->
                        CircularProgressIndicator(Modifier.align(Alignment.Center))

                    is PatientsUiState.Error ->
                        ErrorContent(
                            message = state.message,
                            onRetry = { viewModel.loadPatients() },
                            modifier = Modifier.align(Alignment.Center),
                        )

                    is PatientsUiState.Success ->
                        PatientList(
                            patients = state.patients,
                            onClick = { patientToEdit = it },
                            onShowConsultations = onShowConsultations,
                            onDelete = { viewModel.deletePatient(it) },
                        )
                }
            }
        }
    }

    // Même boîte de dialogue pour ajouter (patient == null) et modifier
    if (showAddDialog || patientToEdit != null) {
        AddPatientDialog(
            patient = patientToEdit,
            onDismiss = {
                showAddDialog = false
                patientToEdit = null
            },
            onConfirm = { request ->
                val editing = patientToEdit
                if (editing == null) viewModel.addPatient(request)
                else viewModel.updatePatient(editing.id, request)
                showAddDialog = false
                patientToEdit = null
            },
        )
    }
}

// Exercice 2 : trois FilterChip. "Tous" = null -> Retrofit n'ajoute pas ?statut=
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatutFilterRow(selected: String?, onSelect: (String?) -> Unit) {
    Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text("Tous") },
        )
        FilterChip(
            selected = selected == "Actif",
            onClick = { onSelect("Actif") },
            label = { Text("Actif") },
        )
        FilterChip(
            selected = selected == "Inactif",
            onClick = { onSelect("Inactif") },
            label = { Text("Inactif") },
        )
    }
}

@Composable
fun ErrorContent(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("Réessayer") }
    }
}

@Composable
fun PatientList(
    patients: List<Patient>,
    onClick: (Patient) -> Unit,
    onShowConsultations: (Patient) -> Unit,
    onDelete: (Patient) -> Unit,
) {
    if (patients.isEmpty()) {
        Text("Aucun patient", modifier = Modifier.padding(24.dp))
    } else {
        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(patients, key = { it.id }) { patient ->
                PatientCard(
                    patient = patient,
                    onClick = { onClick(patient) },
                    onShowConsultations = { onShowConsultations(patient) },
                    onDelete = { onDelete(patient) },
                )
            }
        }
    }
}

// Exercice 1 : Card(onClick = ...) rend la carte cliquable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientCard(
    patient: Patient,
    onClick: () -> Unit,
    onShowConsultations: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${patient.prenom} ${patient.nom}",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = listOfNotNull(
                        patient.age?.let { "$it ans" },
                        patient.medecin,
                        patient.statut,
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            IconButton(onClick = onShowConsultations) {
                Icon(Icons.Default.DateRange, contentDescription = "Consultations")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Supprimer")
            }
        }
    }
}

// Exercice 1 : paramètre patient (null = ajout, sinon modification avec champs pré-remplis)
@Composable
fun AddPatientDialog(
    onDismiss: () -> Unit,
    onConfirm: (PatientRequest) -> Unit,
    patient: Patient? = null,
) {
    var nom by rememberSaveable(patient?.id) { mutableStateOf(patient?.nom ?: "") }
    var prenom by rememberSaveable(patient?.id) { mutableStateOf(patient?.prenom ?: "") }
    var age by rememberSaveable(patient?.id) { mutableStateOf(patient?.age?.toString() ?: "") }
    var medecin by rememberSaveable(patient?.id) { mutableStateOf(patient?.medecin ?: "") }
    val formulaireValide = nom.isNotBlank() && prenom.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (patient == null) "Nouveau patient" else "Modifier le patient") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nom, onValueChange = { nom = it },
                    label = { Text("Nom *") }, singleLine = true
                )
                OutlinedTextField(
                    value = prenom, onValueChange = { prenom = it },
                    label = { Text("Prénom *") }, singleLine = true
                )
                OutlinedTextField(
                    value = age, onValueChange = { age = it.filter { c -> c.isDigit() } },
                    label = { Text("Âge") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = medecin, onValueChange = { medecin = it },
                    label = { Text("Médecin") }, singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = formulaireValide,
                onClick = {
                    onConfirm(
                        PatientRequest(
                            nom = nom.trim(),
                            prenom = prenom.trim(),
                            age = age.toIntOrNull(),
                            // on garde le téléphone existant : il n'est pas dans le formulaire
                            telephone = patient?.telephone,
                            medecin = medecin.trim().ifBlank { null }
                        )
                    )
                }
            ) { Text("Enregistrer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}