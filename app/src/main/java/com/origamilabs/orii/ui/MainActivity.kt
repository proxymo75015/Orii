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
import androidx.lifecycle.lifecycleScope
import com.origamilabs.orii.R
import com.origamilabs.orii.databinding.MainActivityBinding
import com.origamilabs.orii.ui.main.alerts.AlertsFragment
import com.origamilabs.orii.ui.main.help.HelpFragment
import com.origamilabs.orii.ui.main.home.HomeFragment
import com.origamilabs.orii.ui.main.settings.SettingsFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

// Interface top-level pour la délégation de la demande de permission
interface PermissionRequestDelegate {
    fun requestBluetoothPermission()
}

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), PermissionRequestDelegate {

    companion object {
        private const val SELECTED_TAB_INDEX = "selected_tab_index"
    }

    private lateinit var binding: MainActivityBinding
    private val sharedViewModel: SharedViewModel by viewModels()
    private var currentTabIndex: Int = 0
    private lateinit var bluetoothPermissionLauncher: ActivityResultLauncher<Array<String>>

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

        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.viewModel = sharedViewModel

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        initTabs()
        setupBluetoothPermissionLauncher()

        lifecycleScope.launch {
            checkBluetoothPermissions()
        }
    }

    private fun setupBluetoothPermissionLauncher() {
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
    }

    private fun checkBluetoothPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            ).filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }.let { permissionsToRequest.addAll(it) }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        listOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_PHONE_STATE
        ).filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.let { permissionsToRequest.addAll(it) }

        if (permissionsToRequest.isNotEmpty()) {
            bluetoothPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            Timber.d("Toutes les permissions nécessaires sont déjà accordées.")
        }
    }

    private fun initTabs() {
        binding.tabs.removeAllTabs()
        binding.tabs.apply {
            addTab(newTab().setText(R.string.tab_home).setTag("home"))
            addTab(newTab().setText(R.string.tab_alerts).setTag("alerts"))
            addTab(newTab().setText(R.string.tab_settings).setTag("settings"))
            addTab(newTab().setText(R.string.tab_help).setTag("help"))
            addOnTabSelectedListener(onTabSelectedListener)
            getTabAt(currentTabIndex)?.select()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SELECTED_TAB_INDEX, currentTabIndex)
    }

    // Implémentation de l'interface PermissionRequestDelegate.
    // Cette méthode sera appelée par d'autres composants (ex. A2dpHandler) pour demander la permission BLUETOOTH_CONNECT.
    override fun requestBluetoothPermission() {
        bluetoothPermissionLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH_CONNECT))
    }
}
