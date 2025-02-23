package com.origamilabs.orii.ui.main.help

import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.origamilabs.orii.R
import android.widget.Button

/**
 * Activité permettant de recueillir un retour d'information (feedback).
 *
 * Cette activité utilise [FeedbackViewModel] pour traiter la soumission du feedback.
 */
class FeedbackActivity : AppCompatActivity() {

    private lateinit var viewModel: FeedbackViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        // Configuration de la Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialisation du ViewModel via ViewModelProvider (méthode moderne)
        viewModel = ViewModelProvider(this).get(FeedbackViewModel::class.java)

        // Configuration du bouton de soumission
        findViewById<Button>(R.id.submit_button).setOnClickListener {
            val feedbackInput = findViewById<TextInputEditText>(R.id.feedback_input)
            viewModel.handleFeedbackSubmit(this, feedbackInput.text.toString())
        }
    }

    /**
     * Affiche une boîte de dialogue indiquant que le feedback a été envoyé avec succès.
     * Lorsque l'utilisateur ferme la boîte de dialogue, l'activité se termine.
     */
    fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.feedback_post_success_title)
            .setMessage(R.string.feedback_post_success_message)
            .setPositiveButton(R.string.feedback_post_success_close) { _: DialogInterface, _: Int ->
                finish()
            }
            .show()
    }

    /**
     * Affiche un message d'erreur sur le champ de saisie du feedback.
     *
     * @param errorMsg Le message d'erreur à afficher.
     */
    fun setFeedbackError(errorMsg: String) {
        findViewById<TextInputEditText>(R.id.feedback_input).error = errorMsg
    }
}
