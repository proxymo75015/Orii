package com.origamilabs.orii.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import com.origamilabs.orii.R

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Le layout activity_main.xml contient un NavHostFragment qui charge le nav_graph
        setContentView(R.layout.activity_main)
    }
}
