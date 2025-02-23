package com.origamilabs.orii.ui.main.help

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.origamilabs.orii.R
import com.origamilabs.orii.databinding.HelpFragmentBinding
import com.origamilabs.orii.manager.AnalyticsManager
import com.origamilabs.orii.manager.AppManager
import com.origamilabs.orii.ui.tutorial.TutorialActivity
import com.origamilabs.orii.ui.main.help.FirmwareTestActivity
import com.origamilabs.orii.ui.main.help.CatchLogActivity
import com.origamilabs.orii.ui.main.help.WebsiteActivity

class HelpFragment : Fragment() {

    private var binding: HelpFragmentBinding? = null
    private var enableFirmwareTestModeCount = 0
    private var viewModel: HelpViewModel? = null

    companion object {
        private const val FIRMWARE_TEST_MODE_COUNT = 10
        @JvmStatic
        fun newInstance() = HelpFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.help_fragment, container, false)
        return binding?.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(HelpViewModel::class.java)

        binding?.apply {
            // Lorsqu'on clique sur le CardView "Tutorial" : lancement de l'activité TutorialActivity
            cardViewTutorial?.setOnClickListener {
                startActivity(Intent(context, TutorialActivity::class.java))
            }

            // Lorsqu'on clique sur le CardView "Support" : ouverture du site Web de support
            cardViewSupport?.setOnClickListener {
                val intent = Intent(context, WebsiteActivity::class.java).apply {
                    putExtra("websiteUrl", getString(R.string.faq_support_orii_website_url))
                }
                startActivity(intent)
                AnalyticsManager.logAdditionalSupport()
            }

            // Lorsqu'on clique sur le CardView "Feedback" : ouverture du site Web de feedback
            cardViewFeedback?.setOnClickListener {
                val intent = Intent(context, WebsiteActivity::class.java).apply {
                    putExtra("websiteUrl", getString(R.string.feedback_orii_website_url))
                }
                startActivity(intent)
                AnalyticsManager.logFeedback()
            }

            // Lorsqu'on clique sur le CardView "Privacy Policy" : ouverture du site Web de la politique de confidentialité
            cardViewPrivacyPolicy?.setOnClickListener {
                val intent = Intent(context, WebsiteActivity::class.java).apply {
                    putExtra("websiteUrl", getString(R.string.privacy_policy_orii_website_url))
                }
                startActivity(intent)
            }

            // Affichage de la version de l'application
            appVersionTextView?.text = getString(R.string.help_app_version, "2.2.16")

            // Affichage de la version du firmware, ou "N/A" si non disponible
            val firmwareVersion = if (AppManager.firmwareVersion == -1) "N/A" else AppManager.firmwareVersion.toString()
            firmwareTextView?.text = getString(R.string.help_firmware, firmwareVersion)

            // Détection du mode test du firmware : après 10 clics, rendre visible le CardView dédié
            cardViewFirmware?.setOnClickListener {
                enableFirmwareTestModeCount++
                if (enableFirmwareTestModeCount == FIRMWARE_TEST_MODE_COUNT) {
                    cardViewFirmwareTestMode?.visibility = View.VISIBLE
                }
            }

            // Lancement de l'activité de test du firmware
            cardViewFirmwareTestMode?.setOnClickListener {
                startActivity(Intent(context, FirmwareTestActivity::class.java))
            }

            // Par défaut, ne pas afficher le log
            setShowLog(false)

            // Lancement de l'activité pour afficher le log
            cardViewLog?.setOnClickListener {
                startActivity(Intent(context, CatchLogActivity::class.java))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
