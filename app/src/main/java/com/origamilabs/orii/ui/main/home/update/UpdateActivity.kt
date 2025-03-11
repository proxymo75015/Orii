package com.origamilabs.orii.ui.main.home.update

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.origamilabs.orii.R

/**
 * Activité de mise à jour.
 * Les chaînes affichées aux utilisateurs sont en français.
 */
class UpdateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.update_activity)
    }
}
