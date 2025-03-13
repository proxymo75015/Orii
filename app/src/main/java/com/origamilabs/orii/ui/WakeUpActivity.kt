package com.origamilabs.orii.ui

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class WakeUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            // Utilisation des méthodes modernes à partir d'Android 8.1 (API 27)
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            // Pour les anciennes versions, on utilise les flags dépréciés
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        // Ici, vous pouvez afficher une UI ou simplement effectuer l'action de réveil.
        // Si aucune interaction n'est nécessaire, vous pouvez terminer l'Activity.
        finish()
    }
}
