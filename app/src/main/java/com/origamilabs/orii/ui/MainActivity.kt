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
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.origamilabs.orii.R
import com.origamilabs.orii.databinding.MainActivityBinding
import com.origamilabs.orii.ui.main.alerts.AlertsFragment
import com.origamilabs.orii.ui.main.help.HelpFragment
import com.origamilabs.orii.ui.main.home.HomeFragment
import com.origamilabs.orii.ui.main.settings.SettingsFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        private const val SELECTED_TAB_INDEX = "selected_tab_index"
    }

    private lateinit var binding: MainActivityBinding
    private val sharedViewModel: SharedViewModel by viewModels()
    private var currentTabIndex: Int = 0

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

        binding = MainActivityBinding.inflate(layoutInflater).apply {
            lifecycleOwner = this@MainActivity
            viewModel = sharedViewModel
        }

        setContentView(binding.root)

        currentTabIndex = savedInstanceState?.getInt(SELECTED_TAB_INDEX) ?: 0

        setupTabs()
        lifecycleScope.launch {
            requestMissingPermissions()
        }
    }

    private fun requestMissingPermissions() {
        val permissionsNeeded = mutableListOf<String>().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                addAll(arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
                    .filterNot(::isPermissionGranted))
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)
            ) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }

            addAll(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_STATE)
                .filterNot(::isPermissionGranted))
        }

        if (permissionsNeeded.isNotEmpty()) {
            Timber.d("Permissions nécessaires: %s", permissionsNeeded)
            bluetoothPermissionLauncher.launch(permissionsNeeded.toTypedArray())
        } else {
            Timber.d("Toutes les permissions nécessaires sont déjà accordées")
        }
    }

    private fun isPermissionGranted(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    private fun setupTabs() = with(binding.tabs) {
        removeAllTabs()
        addTab(newTab().setText(R.string.tab_home).setTag("home"))
        addTab(newTab().setText(R.string.tab_alerts).setTag("alerts"))
        addTab(newTab().setText(R.string.tab_settings).setTag("settings"))
        addTab(newTab().setText(R.string.tab_help).setTag("help"))

        addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val fragment = when (tab.tag) {
                    "alerts" -> AlertsFragment.newInstance()
                    "settings" -> SettingsFragment.newInstance()
                    "help" -> HelpFragment.newInstance()
                    else -> HomeFragment.newInstance()
                }
                currentTabIndex = tab.position
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, fragment, tab.tag.toString())
                    .commit()

                Timber.d("Onglet sélectionné : %s", tab.tag)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        getTabAt(currentTabIndex)?.select()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SELECTED_TAB_INDEX, currentTabIndex)
    }
}
