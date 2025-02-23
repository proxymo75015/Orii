package com.origamilabs.orii.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.origamilabs.orii.R

class MailSentFragment : Fragment() {

    companion object {
        fun newInstance() = MailSentFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mail_sent, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.findViewById<Button>(R.id.back_button)?.setOnClickListener {
            val navController = Navigation.findNavController(requireActivity(), R.id.fragment)
            navController.navigate(R.id.action_mailSentFragment_to_loginFragment)
        }
    }
}
