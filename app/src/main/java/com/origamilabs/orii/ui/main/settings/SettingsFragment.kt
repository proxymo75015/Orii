package com.origamilabs.orii.ui.main.settings

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.origamilabs.orii.R
import com.origamilabs.orii.databinding.SettingsFragmentBinding
import com.origamilabs.orii.manager.AppManager
import com.origamilabs.orii.models.`enum`.CustomCommandAction
import com.origamilabs.orii.models.`enum`.GestureType
import com.origamilabs.orii.ui.main.SharedViewModel
import com.origamilabs.orii.ui.main.settings.dialog.CustomCommandDialogFragment
import com.origamilabs.orii.ui.main.settings.dialog.WebHookUrlDialogFragment
import com.origamilabs.orii.ui.main.settings.gesture.WebHookTutorialActivity
import java.util.Locale

class SettingsFragment : Fragment() {

    companion object {
        private const val FLAT_TRIPLE_TAP_CUSTOM_COMMAND_TAG = "FLAT_TRIPLE_TAP_CUSTOM_COMMAND_TAG"
        private const val REVERSE_DOUBLE_TAP_CUSTOM_COMMAND_TAG = "REVERSE_DOUBLE_TAP_CUSTOM_COMMAND_TAG"
        private const val TAG = "SettingsFragment"

        @JvmStatic
        fun newInstance(): SettingsFragment = SettingsFragment()
    }

    private lateinit var binding: SettingsFragmentBinding
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                var locale = Locale.getDefault()
                if (locale.language.equals("zh", ignoreCase = true) &&
                    locale.country.equals("HK", ignoreCase = true)
                ) {
                    locale = Locale("yue", "HK")
                }
                Log.d(TAG, "Selected language: $locale")
                tts.language = locale
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.settings_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.lifecycleOwner = this
        settingsViewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
        binding.settingsViewModel = settingsViewModel
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        binding.sharedViewModel = sharedViewModel

        initView()
        initListener()
        cancelSwitchSwipeAction()
    }

    override fun onPause() {
        super.onPause()
        tts.stop()
    }

    override fun onDetach() {
        super.onDetach()
        tts.shutdown()
    }

    private fun initView() {
        val readoutSpeedSeekBar = binding.root.findViewById<SeekBar>(R.id.readout_speed_seek_bar)
        readoutSpeedSeekBar.progress =
            ((AppManager.INSTANCE.sharedPreferences.readoutSpeed * 10) - 5).toInt()

        val gestureFlatTripleTapTextView =
            binding.root.findViewById<TextView>(R.id.gesture_flat_triple_tap_content_text_view)
        val flatTapText =
            "<u>${getString(settingsViewModel.flatTripleTapActionFromPreferences.resId)}</u>"
        gestureFlatTripleTapTextView.text = Html.fromHtml(flatTapText)

        val gestureReverseDoubleTapTextView =
            binding.root.findViewById<TextView>(R.id.gesture_reverse_double_tap_content_text_view)
        val reverseTapText =
            "<u>${getString(settingsViewModel.reverseDoubleTapActionFromPreferences.resId)}</u>"
        gestureReverseDoubleTapTextView.text = Html.fromHtml(reverseTapText)

        updateGesturesControllerState()
    }

    private fun initListener() {
        val readoutSpeedSeekBar = binding.root.findViewById<SeekBar>(R.id.readout_speed_seek_bar)
        readoutSpeedSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Aucune action requise
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Aucune action requise
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val speed = (progress + 5) / 10.0f
                    settingsViewModel.saveReadoutSpeedToPreferences(speed)
                    tts.setSpeechRate(speed)
                    tts.speak(getString(R.string.tts_test_message), TextToSpeech.QUEUE_FLUSH, null, null)
                }
            }
        })

        binding.root.findViewById<ImageView>(R.id.mic_mode_left_image_view).setOnClickListener {
            settingsViewModel.setMicMode(SettingsViewModel.MicMode.LEFT)
        }

        binding.root.findViewById<ImageView>(R.id.mic_mode_right_image_view).setOnClickListener {
            settingsViewModel.setMicMode(SettingsViewModel.MicMode.RIGHT)
        }

        binding.root.findViewById<AppCompatCheckBox>(R.id.gestures_controller_check_box)
            .setOnClickListener { view ->
                val checkBox = view as? CheckBox
                    ?: throw TypeCastException("La vue n'est pas un CheckBox")
                setGlobalGestureMode(checkBox.isChecked)
                changeGestureSwitchCheck(checkBox.isChecked)
            }

        binding.root.findViewById<SwitchCompat>(R.id.gesture_up_double_tap_switch).setOnClickListener {
            changeCallControlSwitch()
            setGestureMode()
            updateGesturesControllerState()
        }

        binding.root.findViewById<SwitchCompat>(R.id.gesture_down_double_tap_switch).setOnClickListener {
            changeCallControlSwitch()
            setGestureMode()
            updateGesturesControllerState()
        }

        binding.root.findViewById<SwitchCompat>(R.id.gesture_flat_double_tap_switch).setOnClickListener {
            setGestureMode()
            updateGesturesControllerState()
        }

        binding.root.findViewById<SwitchCompat>(R.id.gesture_side_double_tap_switch).setOnClickListener {
            setGestureMode()
            updateGesturesControllerState()
        }

        binding.root.findViewById<ImageView>(R.id.gesture_flat_triple_tap_question_mark_image_view)
            .setOnClickListener {
                startWebHookTutorialActivity()
            }

        binding.root.findViewById<SwitchCompat>(R.id.gesture_flat_triple_tap_switch).setOnClickListener {
            setGestureMode()
            updateGesturesControllerState()
        }

        binding.root.findViewById<TextView>(R.id.gesture_flat_triple_tap_content_text_view)
            .setOnClickListener {
                showCustomCommandDialog(FLAT_TRIPLE_TAP_CUSTOM_COMMAND_TAG)
            }

        binding.root.findViewById<TextView>(R.id.gesture_flat_triple_tap_web_hook_url_text_view)
            .setOnClickListener {
                showWebHookUrlDialog(FLAT_TRIPLE_TAP_CUSTOM_COMMAND_TAG)
            }

        binding.root.findViewById<ImageView>(R.id.gesture_reverse_double_tap_question_mark_image_view)
            .setOnClickListener {
                startWebHookTutorialActivity()
            }

        binding.root.findViewById<SwitchCompat>(R.id.gesture_reverse_double_tap_switch).setOnClickListener {
            setGestureMode()
            updateGesturesControllerState()
        }

        binding.root.findViewById<TextView>(R.id.gesture_reverse_double_tap_content_text_view)
            .setOnClickListener {
                showCustomCommandDialog(REVERSE_DOUBLE_TAP_CUSTOM_COMMAND_TAG)
            }

        binding.root.findViewById<TextView>(R.id.gesture_reverse_double_tap_web_hook_url_text_view)
            .setOnClickListener {
                showWebHookUrlDialog(REVERSE_DOUBLE_TAP_CUSTOM_COMMAND_TAG)
            }

        binding.root.findViewById<SwitchCompat>(R.id.gesture_call_control_switch).setOnClickListener {
            setGestureMode()
            updateGesturesControllerState()
        }

        binding.root.findViewById<SeekBar>(R.id.gesture_tap_strength_seek_bar)
            .setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    seekBar?.let {
                        settingsViewModel.setSensitivityOfGesture(it.progress)
                    }
                }
            })
    }

    private fun cancelSwitchSwipeAction() {
        val touchListener = View.OnTouchListener { _, event ->
            event.actionMasked == MotionEvent.ACTION_MOVE
        }
        binding.root.findViewById<SwitchCompat>(R.id.gesture_up_double_tap_switch)
            .setOnTouchListener(touchListener)
        binding.root.findViewById<SwitchCompat>(R.id.gesture_down_double_tap_switch)
            .setOnTouchListener(touchListener)
        binding.root.findViewById<SwitchCompat>(R.id.gesture_flat_double_tap_switch)
            .setOnTouchListener(touchListener)
        binding.root.findViewById<SwitchCompat>(R.id.gesture_side_double_tap_switch)
            .setOnTouchListener(touchListener)
        binding.root.findViewById<SwitchCompat>(R.id.gesture_flat_triple_tap_switch)
            .setOnTouchListener(touchListener)
        binding.root.findViewById<SwitchCompat>(R.id.gesture_reverse_double_tap_switch)
            .setOnTouchListener(touchListener)
        binding.root.findViewById<SwitchCompat>(R.id.gesture_call_control_switch)
            .setOnTouchListener(touchListener)
    }

    fun changeCallControlSwitch() {
        val callControlSwitch =
            binding.root.findViewById<SwitchCompat>(R.id.gesture_call_control_switch)
        val upSwitch = binding.root.findViewById<SwitchCompat>(R.id.gesture_up_double_tap_switch)
        callControlSwitch.isChecked = if (!upSwitch.isChecked) {
            binding.root.findViewById<SwitchCompat>(R.id.gesture_down_double_tap_switch).isChecked
        } else {
            true
        }
    }

    fun setGlobalGestureMode(isChecked: Boolean) {
        if (isChecked) {
            settingsViewModel.setGestureMode(127)
        } else {
            settingsViewModel.setGestureMode(0)
        }
        updateGesturesControllerState()
    }

    fun updateGesturesControllerState() {
        val gesturesOn = settingsViewModel.checkGesturesModeAreOn()
        val gesturesOff = settingsViewModel.checkGesturesModeAreOff()
        val isControllerOn = if (gesturesOn) true else if (gesturesOff) false else false

        val gesturesControllerTextView =
            binding.root.findViewById<TextView>(R.id.gestures_controller_text_view)
        gesturesControllerTextView.text = if (isControllerOn)
            getString(R.string.setting_gestures_controller_on)
        else
            getString(R.string.setting_gestures_controller_off)

        val color = ContextCompat.getColor(
            requireActivity().baseContext,
            if (gesturesOn) R.color.colorGreen else R.color.colorBlack
        )
        gesturesControllerTextView.setTextColor(color)

        binding.root.findViewById<AppCompatCheckBox>(R.id.gestures_controller_check_box).isChecked =
            isControllerOn
    }

    fun changeGestureSwitchCheck(enable: Boolean) {
        binding.root.findViewById<SwitchCompat>(R.id.gesture_call_control_switch).isChecked = enable
        binding.root.findViewById<SwitchCompat>(R.id.gesture_flat_triple_tap_switch).isChecked = enable
        binding.root.findViewById<SwitchCompat>(R.id.gesture_reverse_double_tap_switch).isChecked = enable
        binding.root.findViewById<SwitchCompat>(R.id.gesture_side_double_tap_switch).isChecked = enable
        binding.root.findViewById<SwitchCompat>(R.id.gesture_flat_double_tap_switch).isChecked = enable
        binding.root.findViewById<SwitchCompat>(R.id.gesture_down_double_tap_switch).isChecked = enable
        binding.root.findViewById<SwitchCompat>(R.id.gesture_up_double_tap_switch).isChecked = enable
    }

    fun setGestureMode() {
        var bit = if (binding.root.findViewById<SwitchCompat>(R.id.gesture_call_control_switch).isChecked)
            GestureType.CALL_CONTROL.bit else 0

        if (binding.root.findViewById<SwitchCompat>(R.id.gesture_flat_triple_tap_switch).isChecked) {
            bit += GestureType.FLAT_TRIPLE_TAP.bit
        }
        if (binding.root.findViewById<SwitchCompat>(R.id.gesture_reverse_double_tap_switch).isChecked) {
            bit += GestureType.REVERSE_DOUBLE_TAP.bit
        }
        if (binding.root.findViewById<SwitchCompat>(R.id.gesture_side_double_tap_switch).isChecked) {
            bit += GestureType.SIDE_DOUBLE_TAP.bit
        }
        if (binding.root.findViewById<SwitchCompat>(R.id.gesture_flat_double_tap_switch).isChecked) {
            bit += GestureType.FLAT_DOUBLE_TAP.bit
        }
        if (binding.root.findViewById<SwitchCompat>(R.id.gesture_down_double_tap_switch).isChecked) {
            bit += GestureType.DOWN_DOUBLE_TAP.bit
        }
        if (binding.root.findViewById<SwitchCompat>(R.id.gesture_up_double_tap_switch).isChecked) {
            bit += GestureType.UP_DOUBLE_TAP.bit
        }
        settingsViewModel.setGestureMode(bit)
    }

    fun showWebHookUrlDialog(tag: String) {
        val url = when (tag) {
            FLAT_TRIPLE_TAP_CUSTOM_COMMAND_TAG -> settingsViewModel.flatTripleTapWebHookUrlFromPreferences
            REVERSE_DOUBLE_TAP_CUSTOM_COMMAND_TAG -> settingsViewModel.reverseDoubleTapWebHookUrlFromPreferences
            else -> ""
        }
        val webHookUrlDialogFragment = WebHookUrlDialogFragment(url)
        webHookUrlDialogFragment.setOnDialogEnterListener { inputUrl ->
            if (webHookUrlDialogFragment.tag == FLAT_TRIPLE_TAP_CUSTOM_COMMAND_TAG) {
                settingsViewModel.saveFlatTripleTapWebHookUrlToPreferences(inputUrl)
            } else if (webHookUrlDialogFragment.tag == REVERSE_DOUBLE_TAP_CUSTOM_COMMAND_TAG) {
                settingsViewModel.saveReverseDoubleTapWebHookUrlToPreferences(inputUrl)
            }
        }
        fragmentManager?.let { webHookUrlDialogFragment.show(it, tag) }
    }

    fun showCustomCommandDialog(tag: String) {
        val customCommandAction = when (tag) {
            FLAT_TRIPLE_TAP_CUSTOM_COMMAND_TAG -> settingsViewModel.flatTripleTapActionFromPreferences
            REVERSE_DOUBLE_TAP_CUSTOM_COMMAND_TAG -> settingsViewModel.reverseDoubleTapActionFromPreferences
            else -> CustomCommandAction.WEB_HOOK
        }
        val customCommandDialogFragment = CustomCommandDialogFragment(customCommandAction)
        customCommandDialogFragment.setOnDialogEnterListener { action ->
            if (customCommandDialogFragment.tag == FLAT_TRIPLE_TAP_CUSTOM_COMMAND_TAG) {
                settingsViewModel.saveFlatTripleTapActionToPreferences(action)
                binding.root.findViewById<TextView>(R.id.gesture_flat_triple_tap_content_text_view).text =
                    Html.fromHtml("<u>${getString(action.resId)}</u>")
            } else if (customCommandDialogFragment.tag == REVERSE_DOUBLE_TAP_CUSTOM_COMMAND_TAG) {
                settingsViewModel.saveReverseDoubleTapActionToPreferences(action)
                binding.root.findViewById<TextView>(R.id.gesture_reverse_double_tap_content_text_view).text =
                    Html.fromHtml("<u>${getString(action.resId)}</u>")
            }
        }
        fragmentManager?.let { customCommandDialogFragment.show(it, tag) }
    }

    fun startWebHookTutorialActivity() {
        startActivity(Intent(context, WebHookTutorialActivity::class.java))
    }
}
