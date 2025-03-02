package com.origamilabs.orii.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activité principale lancée au démarrage de l’application.
 * Contient le fragment principal de l’application.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // Si l'application utilise un NavHostFragment, cette activité fait office de container.
    // (Le ViewModel peut être utilisé ici si nécessaire, par ex. pour des fragments multiples)
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Plus besoin de vérifier l’authentification : on affiche directement le contenu principal.
        // L'interface (fragment) principale sera affichée via le NavHostFragment défini dans activity_main.xml.
    }
}
