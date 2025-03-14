package com.origamilabs.orii.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.origamilabs.orii.utils.ResourceProvider
import android.annotation.SuppressLint

class DialerActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CALL_PERMISSION = 1
    }

    private lateinit var resourceProvider: ResourceProvider
    private lateinit var numberDisplay: MaterialTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resourceProvider = ResourceProvider(this)

        // Récupération de l'ID de la mise en page "activity_dialer" de manière dynamique
        val layoutId = getIdentifier("activity_dialer", "layout")
        setContentView(layoutId)

        // Récupération dynamique de l'ID de numberDisplay
        val numberDisplayId = getIdentifier("numberDisplay", "id")
        numberDisplay = findViewById(numberDisplayId)

        // Récupération dynamique des boutons du pavé numérique
        val dialPadButtonNames = listOf(
            "button1", "button2", "button3", "button4",
            "button5", "button6", "button7", "button8",
            "button9", "buttonStar", "button0", "buttonHash"
        )
        val dialPadButtons = dialPadButtonNames.map { name ->
            val id = getIdentifier(name, "id")
            findViewById<MaterialButton>(id)
        }

        // Affecte l'action de concaténer le texte du bouton à l'affichage
        dialPadButtons.forEach { button ->
            button.setOnClickListener {
                val digit = button.text.toString()
                val currentText = numberDisplay.text.toString()
                // Récupère l'ID de la ressource pour la chaîne formatée
                val formatId = getIdentifier("dialer_number_format", "string")
                // Utilise le string resource avec placeholders pour concaténer currentText et digit
                numberDisplay.text = resourceProvider.getString(formatId, currentText, digit)
            }
        }

        // Bouton Effacer : supprime le dernier caractère
        val deleteButtonId = getIdentifier("deleteButton", "id")
        findViewById<MaterialButton>(deleteButtonId).setOnClickListener {
            val currentText = numberDisplay.text.toString()
            if (currentText.isNotEmpty()) {
                numberDisplay.text = currentText.dropLast(1)
            }
        }

        // Bouton Appeler : vérifie les permissions et lance l'appel
        val callButtonId = getIdentifier("callButton", "id")
        findViewById<MaterialButton>(callButtonId).setOnClickListener {
            val phoneNumber = numberDisplay.text.toString()
            if (phoneNumber.isNotBlank()) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.CALL_PHONE),
                        REQUEST_CALL_PERMISSION
                    )
                } else {
                    makeCall(phoneNumber)
                }
            }
        }
    }

    /**
     * Méthode utilitaire pour récupérer l'ID d'une ressource par son nom et son type.
     */
    @SuppressLint("DiscouragedApi")
    private fun getIdentifier(name: String, defType: String): Int {
        val id = resourceProvider.context.resources.getIdentifier(name, defType, packageName)
        require(id != 0) { "La ressource \"$name\" de type \"$defType\" n'a pas été trouvée." }
        return id
    }

    private fun makeCall(phoneNumber: String) {
        val callIntent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        startActivity(callIntent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CALL_PERMISSION && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val phoneNumber = numberDisplay.text.toString()
            if (phoneNumber.isNotBlank()) {
                makeCall(phoneNumber)
            }
        }
    }
}
