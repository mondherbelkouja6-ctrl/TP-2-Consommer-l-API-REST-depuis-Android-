package com.example.tp2

import android.app.Application
import com.example.tp2.di.AppContainer

class PatientsApplication : Application() {
    // Créé une seule fois, partagé par toute l'application
    val container by lazy { AppContainer() }
}
