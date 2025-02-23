package com.origamilabs.orii.ui.main.settings.dialog

import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.origamilabs.orii.R
import com.origamilabs.orii.ui.main.settings.gesture.WebHookTutorialActivity

/**
 * DialogFragment pour saisir une URL de WebHook.
 *
 * @property url L'URL par défaut à afficher dans le champ de saisie.
 * Vous pouvez définir un listener via [setOnDialogEnterListener] pour recevoir
 * l'URL saisie lorsque l'utilisateur clique sur le bouton "Enter".
 */
class WebHookUrlDialogFragment(private val url: String) : DialogFragment() {

    private var dialogEnterListener: OnDialogEnterListener? = null

    private lateinit var dialogWebHookCancelButton: Button
    private lateinit var dialogWebHookEnterButton: Button
    private lateinit var dialogWebHookQuestionMarkImageView: ImageView
    private lateinit var dialogWebHookUrlClearImageView: ImageView
    private lateinit var dialogWebHookUrlEditText: EditText

    /**
     * Interface permettant de recevoir l'URL saisie par l'utilisateur.
     */
    interface OnDialogEnterListener {
        fun onDialogEnter(url: String)
    }

    /**
     * Définit le listener à appeler lorsque l'utilisateur clique sur "Enter".
     */
    fun setOnDialogEnterListener(listener: OnDialogEnterListener) {
        dialogEnterListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val builder = AlertDialog.Builder(context)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_setting_gesture_web_hook_url, null)
        initView(view)
        initListener()
        builder.setView(view)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(0))
        isCancelable = false
        return dialog
    }

    private fun initView(view: View) {
        dialogWebHookQuestionMarkImageView = view.findViewById(R.id.dialog_web_hook_question_mark_image_view)
        dialogWebHookUrlClearImageView = view.findViewById(R.id.dialog_web_hook_url_clear_image_view)
        dialogWebHookUrlEditText = view.findViewById(R.id.dialog_web_hook_url_edit_text)
        dialogWebHookCancelButton = view.findViewById(R.id.dialog_web_hook_cancel_button)
        dialogWebHookEnterButton = view.findViewById(R.id.dialog_web_hook_enter_button)
        dialogWebHookUrlEditText.setText(url)
    }

    private fun initListener() {
        dialogWebHookQuestionMarkImageView.setOnClickListener {
            startWebHookTutorialActivity()
        }
        dialogWebHookUrlClearImageView.setOnClickListener {
            dialogWebHookUrlEditText.text.clear()
        }
        dialogWebHookCancelButton.setOnClickListener {
            dismiss()
        }
        dialogWebHookEnterButton.setOnClickListener {
            val enteredUrl = dialogWebHookUrlEditText.text.toString()
            dialogEnterListener?.onDialogEnter(enteredUrl)
            dismiss()
        }
    }

    fun startWebHookTutorialActivity() {
        startActivity(Intent(requireContext(), WebHookTutorialActivity::class.java))
    }
}
