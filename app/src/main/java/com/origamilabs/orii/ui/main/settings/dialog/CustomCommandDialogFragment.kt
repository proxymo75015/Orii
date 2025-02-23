package com.origamilabs.orii.ui.main.settings.dialog

import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.origamilabs.orii.R
import com.origamilabs.orii.models.enum.CustomCommandAction
import com.origamilabs.orii.receiver.AdminReceiver

/**
 * DialogFragment permettant de configurer une action personnalisée pour un geste.
 *
 * Le paramètre [customCommandAction] indique l'action par défaut à sélectionner.
 *
 * Vous pouvez définir un listener via [setOnDialogEnterListener] pour recevoir la valeur choisie
 * lorsque l'utilisateur clique sur le bouton "Enter".
 */
class CustomCommandDialogFragment(
    private val customCommandAction: CustomCommandAction
) : DialogFragment() {

    private var dialogEnterListener: OnDialogEnterListener? = null

    private lateinit var dialogCustomCommandRadioGroup: RadioGroup
    private lateinit var customCommandOption3RadioButton: RadioButton
    private lateinit var dialogCustomCommandEnterButton: Button
    private lateinit var dialogCustomCommandCancelButton: Button

    /**
     * Interface permettant de recevoir l'action choisie par l'utilisateur.
     */
    interface OnDialogEnterListener {
        fun onDialogEnter(customCommandAction: CustomCommandAction)
    }

    /**
     * Définit le listener à appeler lorsque l'utilisateur clique sur "Enter".
     */
    fun setOnDialogEnterListener(listener: OnDialogEnterListener) {
        dialogEnterListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context: Context = requireContext()
        val builder = AlertDialog.Builder(context)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_setting_gesture_custom_command, null)

        initView(view)
        initListener()

        builder.setView(view)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(0))
        isCancelable = false
        return dialog
    }

    private fun initView(view: View) {
        dialogCustomCommandRadioGroup = view.findViewById(R.id.dialog_custom_command_radio_group)
        customCommandOption3RadioButton = view.findViewById(R.id.custom_command_option_3_radio_button)
        dialogCustomCommandEnterButton = view.findViewById(R.id.dialog_custom_command_enter_button)
        dialogCustomCommandCancelButton = view.findViewById(R.id.dialog_custom_command_cancel_button)
        
        // Calcule l'indice du RadioButton à sélectionner : (ordinal * 2) + 1
        val indexToSelect = (customCommandAction.ordinal * 2) + 1
        val radioButtonToSelect = dialogCustomCommandRadioGroup.getChildAt(indexToSelect) as? RadioButton
            ?: throw TypeCastException("Le RadioButton à l'indice $indexToSelect n'est pas disponible.")
        radioButtonToSelect.isChecked = true
    }

    private fun initListener() {
        // Lorsqu'on change l'état du bouton radio custom_command_option_3, on lance une demande d'activation d'administration.
        customCommandOption3RadioButton.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                activity?.let { act ->
                    val intent = Intent("android.app.action.ADD_DEVICE_ADMIN")
                        .putExtra("android.app.extra.DEVICE_ADMIN", ComponentName(act, AdminReceiver::class.java))
                    act.startActivity(intent)
                }
            }
        }

        // Listener sur le bouton "Enter"
        dialogCustomCommandEnterButton.setOnClickListener {
            // Récupère l'index du RadioButton sélectionné
            val checkedRadioButtonId = dialogCustomCommandRadioGroup.checkedRadioButtonId
            val checkedRadioButton = dialogCustomCommandRadioGroup.findViewById<RadioButton>(checkedRadioButtonId)
            val index = dialogCustomCommandRadioGroup.indexOfChild(checkedRadioButton)
            // D'après la logique décompilée, l'index du CustomCommandAction correspond à (index - 1) / 2
            val actionIndex = (index - 1) / 2
            val selectedAction = CustomCommandAction.values()[actionIndex]
            dialogEnterListener?.onDialogEnter(selectedAction)
            dismiss()
        }

        // Listener sur le bouton "Cancel"
        dialogCustomCommandCancelButton.setOnClickListener {
            dismiss()
        }
    }
}
