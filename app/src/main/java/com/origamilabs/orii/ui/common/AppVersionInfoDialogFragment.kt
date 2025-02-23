package com.origamilabs.orii.ui.common

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.origamilabs.orii.R

class AppVersionInfoDialogFragment : DialogFragment() {

    var dialogBugFixesText: String? = null
    var dialogNewFeaturesText: String? = null
    private var onDialogClickListener: OnDialogClickListener? = null

    interface OnDialogClickListener {
        fun onDialogCancelClick()
        fun onDialogUpdateClick()
    }

    fun setOnDialogClickListener(listener: OnDialogClickListener) {
        onDialogClickListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Utilise requireContext() pour garantir que le contexte n'est pas null
        val context = requireContext()
        val builder = AlertDialog.Builder(context)
        // Obtention de l'inflater depuis l'activité ou via le LayoutInflater du contexte
        val inflater = activity?.layoutInflater ?: LayoutInflater.from(context)
        // Inflatation du layout de la boîte de dialogue
        val view = inflater.inflate(R.layout.app_version_info_dialog_fragment, null)

        // Récupération des vues nécessaires
        val updateButton: Button = view.findViewById(R.id.app_update_button)
        val cancelButton: Button = view.findViewById(R.id.app_cancel_button)
        val newFeaturesTextView: TextView? = view.findViewById(R.id.app_new_features_text_view)
        val bugFixesTextView: TextView? = view.findViewById(R.id.app_bug_fixes_text_view)

        // Configuration des actions sur les boutons
        updateButton.setOnClickListener {
            onDialogClickListener?.onDialogUpdateClick()
            shown = false
        }
        cancelButton.setOnClickListener {
            onDialogClickListener?.onDialogCancelClick()
            shown = false
        }

        // Mise à jour des TextView avec les textes ou un message par défaut
        newFeaturesTextView?.text = dialogNewFeaturesText ?: "New features is empty"
        bugFixesTextView?.text = dialogBugFixesText ?: "Bug Fixes is empty"

        builder.setView(view)
        isCancelable = false

        return builder.create()
    }

    companion object {
        // Variable statique pour éviter de montrer la boîte de dialogue plusieurs fois
        private var shown = false

        fun newInstance(): AppVersionInfoDialogFragment? {
            return if (shown) {
                null
            } else {
                shown = true
                AppVersionInfoDialogFragment()
            }
        }
    }
}
