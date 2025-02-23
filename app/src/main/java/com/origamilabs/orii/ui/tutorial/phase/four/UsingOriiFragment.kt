package com.origamilabs.orii.ui.tutorial.phase.four

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.generic.GenericDraweeHierarchy
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ImageDecodeOptionsBuilder
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.origamilabs.orii.R
import com.origamilabs.orii.databinding.UsingOriiFragmentBinding
import com.origamilabs.orii.ui.tutorial.TutorialActivity
import com.origamilabs.orii.ui.tutorial.TutorialFragment
import com.origamilabs.orii.utils.SoundTester
import hiennguyen.me.circleseekbar.CircleSeekBar
import kotlin.TypeCastException
import kotlin.Unit
import kotlin.jvm.functions.Function0

class UsingOriiFragment : TutorialFragment() {

    private lateinit var binding: UsingOriiFragmentBinding
    private lateinit var audioManager: AudioManager
    private var soundTester: SoundTester? = null
    private lateinit var viewModel: UsingOriiViewModel
    private var volumeRate: Double = 0.0

    companion object {
        private const val TAG = "UsingOriiFragment"
        @JvmStatic
        fun newInstance(): UsingOriiFragment = UsingOriiFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.using_orii_fragment, container, false)
        return binding.root
    }

    /**
     * Met à jour l'état de lecture lorsque la visibilité du fragment change.
     */
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            binding.isPlaying = false
        } else {
            soundTester?.stopAudio()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Récupération du ViewModel
        viewModel = ViewModelProviders.of(this).get(UsingOriiViewModel::class.java)

        // Configuration du gifView avec Fresco
        val gifView = view?.findViewById<SimpleDraweeView>(R.id.gifView)
            ?: throw NullPointerException("gifView not found")
        val hierarchy: GenericDraweeHierarchy = gifView.hierarchy
        hierarchy.actualImageScaleType = ScalingUtils.ScaleType.CENTER_CROP

        val imageRequest: ImageRequest = ImageRequestBuilder.newBuilderWithResourceId(R.raw.using_orii)
            .setImageDecodeOptions(ImageDecodeOptionsBuilder().setForceStaticImage(true).build())
            .build()
        val sourceUri: Uri = imageRequest.sourceUri
        val controllerBuilder: PipelineDraweeControllerBuilder =
            Fresco.getDraweeControllerBuilderSupplier().get()
        val oldController = gifView.controller
        val controller = controllerBuilder.setOldController(oldController)
            .setUri(sourceUri)
            .setAutoPlayAnimations(true)
            .build()
        gifView.controller = controller

        // Récupération du service audio
        val activity: FragmentActivity = requireActivity()
        audioManager = activity.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: throw TypeCastException("null cannot be cast to non-null type AudioManager")

        // Initialisation du SoundTester
        val ctx: Context = requireContext()
        soundTester = SoundTester(ctx) {
            binding.isPlaying = false
        }

        // Calcul du volumeRate : 100 / volume maximum du stream music (3)
        volumeRate = 100.0 / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        // Mise à jour du CircleSeekBar avec le volume actuel
        val circleSeekBar = view?.findViewById<CircleSeekBar>(R.id.progressBar)
            ?: throw NullPointerException("progressBar not found")
        circleSeekBar.setProgressDisplayAndInvalidate(
            (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * volumeRate).toInt()
        )
        Log.d(TAG, "Progress is ${(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * volumeRate).toInt()}")

        // Configuration du bouton "Next"
        view?.findViewById<Button>(R.id.next_text_button)?.setOnClickListener {
            navigateToNext()
        }

        // Configuration du bouton play/pause
        view?.findViewById<ImageView>(R.id.play_pause_image_view)?.setOnClickListener {
            binding.isPlaying = soundTester?.toggleAudio() ?: false
        }

        // Configuration du bouton d'augmentation du volume
        view?.findViewById<ImageView>(R.id.add_image_view)?.setOnClickListener {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 8)
            val csb = view?.findViewById<CircleSeekBar>(R.id.progressBar)
                ?: throw NullPointerException("progressBar not found")
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            csb.setProgressDisplayAndInvalidate((currentVolume * volumeRate).toInt())
        }

        // Configuration du bouton de diminution du volume
        view?.findViewById<ImageView>(R.id.minus_image_view)?.setOnClickListener {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 8)
            val csb = view?.findViewById<CircleSeekBar>(R.id.progressBar)
                ?: throw NullPointerException("progressBar not found")
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            csb.setProgressDisplayAndInvalidate((currentVolume * volumeRate).toInt())
        }

        // Configuration du listener du CircleSeekBar
        view?.findViewById<CircleSeekBar>(R.id.progressBar)?.setSeekBarChangeListener(object :
            CircleSeekBar.OnSeekBarChangedListener {
            var isSwipe = false
            override fun onPointsChanged(circleSeekBar: CircleSeekBar, points: Int, fromUser: Boolean) {
                if (isSwipe) {
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        (points / volumeRate).toInt(),
                        0
                    )
                }
            }
            override fun onStartTrackingTouch(circleSeekBar: CircleSeekBar) {
                isSwipe = true
            }
            override fun onStopTrackingTouch(circleSeekBar: CircleSeekBar) {
                isSwipe = false
            }
        })
    }

    fun navigateToNext() {
        Log.d(TAG, "navigateToNext")
        Log.d(TAG, "activity $activity")
        activity?.runOnUiThread {
            val act = activity ?: throw TypeCastException("null cannot be cast to non-null type TutorialActivity")
            (act as TutorialActivity).navigateToNext()
        }
    }
}
