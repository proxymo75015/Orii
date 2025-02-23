package com.origamilabs.orii.ui.main.help

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.origamilabs.orii.R
import com.origamilabs.orii.databinding.ActivityCatchLogBinding

class CatchLogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCatchLogBinding
    private val viewModel: CatchLogViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configure le binding avec le layout activity_catch_log
        binding = DataBindingUtil.setContentView(this, R.layout.activity_catch_log)
        // Fournit le contexte à notre ViewModel
        viewModel.setMContext(this)
        // Lie le ViewModel au binding
        binding.viewModel = viewModel
        // Définit le cycle de vie du binding sur celui de l'activité
        binding.lifecycleOwner = this
    }
}
