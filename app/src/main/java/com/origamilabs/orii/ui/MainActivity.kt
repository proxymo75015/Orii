package com.origamilabs.orii.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.origamilabs.orii.R
import com.origamilabs.orii.databinding.MainActivityBinding
import com.origamilabs.orii.ui.main.alerts.AlertsFragment
import com.origamilabs.orii.ui.main.help.HelpFragment
import com.origamilabs.orii.ui.main.home.HomeFragment
import com.origamilabs.orii.ui.main.settings.SettingsFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        private const val SELECTED_TAB_INDEX = "selected_tab_index"
    }

    // ViewBinding généré depuis main_activity.xml
    private lateinit var binding: MainActivityBinding

    // Injection du ViewModel partagé via Hilt
    private val sharedViewModel: SharedViewModel by viewModels()

    private var currentTabIndex: Int = 0

    // Launcher pour la demande de permissions Bluetooth
    private lateinit var bluetoothPermissionLauncher: ActivityResultLauncher<Array<String>>

    // Listener pour la sélection des onglets
    private val onTabSelectedListener = object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
        override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
        override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab) {
            val tag = tab.tag as? String ?: ""
            Timber.d("Onglet sélectionné : $tag")
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
        currentTabIndex = savedInstanceState?.getInt(SELECTED_TAB_INDEX, 0) ?: 0

        // Initialisation du ViewBinding
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.viewModel = sharedViewModel

        // Active le support des vecteurs
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        // Initialisation des onglets
        initTabs()

        // Enregistrement du launcher pour la demande de permissions
        bluetoothPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permissions.forEach { (permission, isGranted) ->
                if (isGranted) {
                    Timber.d("Permission accordée : $permission")
                } else {
                    Timber.e("Permission refusée : $permission")
                }
            }
        }

        // Vérification des permissions dans une coroutine
        CoroutineScope(Dispatchers.Main).launch {
            checkBluetoothPermissions()
        }
    }

    /**
     * Vérifie et demande les permissions Bluetooth nécessaires pour Android 12+.
     */
    private fun checkBluetoothPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            }
        }
        if (permissionsToRequest.isNotEmpty()) {
            bluetoothPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            Timber.d("Toutes les permissions Bluetooth sont déjà accordées.")
        }
    }

    /**
     * Initialise les onglets de l'interface principale.
     */
    private fun initTabs() {
        binding.tabs.removeAllTabs()

        val tabHome = binding.tabs.newTab().setText(R.string.tab_home).setTag("home")
        val tabAlerts = binding.tabs.newTab().setText(R.string.tab_alerts).setTag("alerts")
        val tabSettings = binding.tabs.newTab().setText(R.string.tab_settings).setTag("settings")
        val tabHelp = binding.tabs.newTab().setText(R.string.tab_help).setTag("help")

        binding.tabs.addTab(tabHome)
        binding.tabs.addTab(tabAlerts)
        binding.tabs.addTab(tabSettings)
        binding.tabs.addTab(tabHelp)

        binding.tabs.addOnTabSelectedListener(onTabSelectedListener)
        binding.tabs.getTabAt(currentTabIndex)?.select()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SELECTED_TAB_INDEX, currentTabIndex)
    }
}
