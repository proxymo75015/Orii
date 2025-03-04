package com.origamilabs.orii.ui.main

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.google.android.material.tabs.TabLayout
import com.origamilabs.orii.R
import com.origamilabs.orii.databinding.MainActivityBinding
import com.origamilabs.orii.ui.main.alerts.AlertsFragment
import com.origamilabs.orii.ui.main.help.HelpFragment
import com.origamilabs.orii.ui.main.home.HomeFragment
import com.origamilabs.orii.ui.main.settings.SettingsFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activité principale de l'application ORII.
 *
 * Gère la navigation par onglets et l’interface principale.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val SELECTED_TAB_INDEX = "selected_tab_index"
    }

    // Binding du layout principal
    private lateinit var binding: MainActivityBinding

    // ViewModel partagé (injection Hilt)
    private val sharedViewModel: SharedViewModel by viewModels()

    // Index de l’onglet sélectionné (pour restauration état)
    private var currentTabIndex: Int = 0

    // Ecouteur de sélection d’onglets
    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab) {}
        override fun onTabUnselected(tab: TabLayout.Tab) {}
        override fun onTabSelected(tab: TabLayout.Tab) {
            val tag = tab.tag as String? ?: ""
            Log.d(TAG, "Onglet sélectionné : $tag")
            // Remplace le fragment affiché en fonction de l'onglet choisi
            val newFragment = when (tag) {
                "alerts"   -> AlertsFragment.newInstance()
                "help"     -> HelpFragment.newInstance()
                "settings" -> SettingsFragment.newInstance()
                else       -> HomeFragment.newInstance()
            }
            currentTabIndex = tab.position
            supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.container, newFragment, tag)
                .commit()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Restaurer l’onglet sélectionné si on recrée l’activité (rotation)
        currentTabIndex = savedInstanceState?.getInt(SELECTED_TAB_INDEX, 0) ?: 0

        // Configuration du DataBinding avec le layout
        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)
        binding.lifecycleOwner = this
        binding.viewModel = sharedViewModel  // le ViewModel partagé peut être lié à l’UI si besoin

        // Activation du support des vecteurs (icônes vectorielles)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        // Initialisation de la barre d’onglets
        initTabs()

        // **Suppression**: plus d'initialisation de Firebase Cloud Messaging ni de login externe.
        // L'application fonctionne sans compte utilisateur ni push notifications externes.
    }

    /**
     * Initialise les onglets du menu principal.
     */
    private fun initTabs() {
        with(binding.tabs) {
            removeAllTabs()
            // Création des onglets Home, Alerts, Settings, Help
            val tabHome = newTab().setText(R.string.tab_home).setTag("home")
            val tabAlerts = newTab().setText(R.string.tab_alerts).setTag("alerts")
            val tabSettings = newTab().setText(R.string.tab_settings).setTag("settings")
            val tabHelp = newTab().setText(R.string.tab_help).setTag("help")

            addTab(tabHome)
            addTab(tabAlerts)
            addTab(tabSettings)
            addTab(tabHelp)

            // Attache le listener de sélection
            addOnTabSelectedListener(onTabSelectedListener)
            // Sélectionne l'onglet courant enregistré
            getTabAt(currentTabIndex)?.select()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Sauvegarde l’index de l’onglet sélectionné pour recréation
        outState.putInt(SELECTED_TAB_INDEX, currentTabIndex)
    }
}
