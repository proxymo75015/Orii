package com.origamilabs.orii.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.tabs.TabLayout
import com.origamilabs.orii.databinding.MainActivityBinding
import com.origamilabs.orii.ui.main.alerts.AlertsFragment
import com.origamilabs.orii.ui.main.help.HelpFragment
import com.origamilabs.orii.ui.main.home.HomeFragment
import com.origamilabs.orii.ui.main.settings.SettingsFragment
import com.origamilabs.orii.utils.ResourceProvider
import com.origamilabs.orii.datastore.SettingsDataStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: MainActivityBinding
    private val sharedViewModel: SharedViewModel by viewModels()
    private var currentTabIndex: Int = 0
    private lateinit var resourceProvider: ResourceProvider

    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.forEach { (permission, isGranted) ->
            Timber.d("Permission %s accordée: %s", permission, isGranted)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        resourceProvider = ResourceProvider(this)

        binding = MainActivityBinding.inflate(layoutInflater).apply {
            lifecycleOwner = this@MainActivity
            viewModel = sharedViewModel
        }
        setContentView(binding.root)

        setupTabs()

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                SettingsDataStore.getLastTabIndex(applicationContext).collectLatest { index ->
                    currentTabIndex = index
                    binding.tabs.getTabAt(currentTabIndex)?.select()
                }
            }
        }

        lifecycleScope.launch {
            requestMissingPermissions()
        }
    }

    private fun setupTabs() = with(binding.tabs) {
        removeAllTabs()
        addTab(newTab().setText(resourceProvider.tabHome).setTag("home"))
        addTab(newTab().setText(resourceProvider.tabAlerts).setTag("alerts"))
        addTab(newTab().setText(resourceProvider.tabSettings).setTag("settings"))
        addTab(newTab().setText(resourceProvider.tabHelp).setTag("help"))

        addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                lifecycleScope.launch {
                    SettingsDataStore.setLastTabIndex(applicationContext, tab.position)
                }
                val fragment = when (tab.tag) {
                    "alerts" -> AlertsFragment.newInstance()
                    "settings" -> SettingsFragment.newInstance()
                    "help" -> HelpFragment.newInstance()
                    else -> HomeFragment.newInstance()
                }
                supportFragmentManager.beginTransaction()
                    .replace(resourceProvider.containerId, fragment, tab.tag.toString())
                    .commit()

                Timber.d("Onglet sélectionné : %s", tab.tag)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        getTabAt(0)?.select()
    }

    private fun isPermissionGranted(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    private fun requestMissingPermissions() {
        val permissionsNeeded = mutableListOf<String>().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                addAll(arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                ).filterNot(::isPermissionGranted))
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)
            ) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
            addAll(arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_PHONE_STATE
            ).filterNot(::isPermissionGranted))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                !isPermissionGranted(Manifest.permission.ANSWER_PHONE_CALLS)
            ) {
                add(Manifest.permission.ANSWER_PHONE_CALLS)
            }
        }

        if (permissionsNeeded.isNotEmpty()) {
            Timber.d("Permissions nécessaires: %s", permissionsNeeded)
            bluetoothPermissionLauncher.launch(permissionsNeeded.toTypedArray())
        } else {
            Timber.d("Toutes les permissions nécessaires sont déjà accordées")
        }
    }
}
