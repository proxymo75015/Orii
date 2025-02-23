package com.origamilabs.orii.ui.tutorial

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.origamilabs.orii.R
import com.origamilabs.orii.databinding.ActivityTutorialBinding
import com.origamilabs.orii.manager.AnalyticsManager
import com.origamilabs.orii.ui.main.MainActivity
import com.origamilabs.orii.ui.main.help.WebsiteActivity
import com.origamilabs.orii.ui.tutorial.phase.five.CallVaFragment
import com.origamilabs.orii.ui.tutorial.phase.five.ReadoutFragment
import com.origamilabs.orii.ui.tutorial.phase.four.CloseEarFragment
import com.origamilabs.orii.ui.tutorial.phase.four.RotateFragment
import com.origamilabs.orii.ui.tutorial.phase.four.UsingOriiFragment
import com.origamilabs.orii.ui.tutorial.phase.one.ConnectionFragment
import com.origamilabs.orii.ui.tutorial.phase.one.TurnOnFragment
import com.origamilabs.orii.ui.tutorial.phase.seven.CompleteFragment
import com.origamilabs.orii.ui.tutorial.phase.six.GestureAnglesFragment
import com.origamilabs.orii.ui.tutorial.phase.six.GestureCustomCommand1Fragment
import com.origamilabs.orii.ui.tutorial.phase.six.GestureCustomCommand2Fragment
import com.origamilabs.orii.ui.tutorial.phase.six.GestureMessageReadoutFragment
import com.origamilabs.orii.ui.tutorial.phase.six.GesturePlayPauseMusicFragment
import com.origamilabs.orii.ui.tutorial.phase.six.GestureSkipTrackFragment
import com.origamilabs.orii.ui.tutorial.phase.six.GestureVoiceAssistantFragment
import com.origamilabs.orii.ui.tutorial.phase.three.RightFitFragment
import com.origamilabs.orii.ui.tutorial.phase.three.ScrewFragment
import com.origamilabs.orii.ui.tutorial.phase.three.UnlockFragment
import com.origamilabs.orii.ui.tutorial.phase.three.UnscrewFragment
import com.origamilabs.orii.ui.tutorial.phase.two.MainMenuFragment
import com.origamilabs.orii.ui.tutorial.phase.zero.InitialFragment
import com.origamilabs.orii.ui.tutorial.phase.zero.OpeningFragment
import com.origamilabs.orii.ui.tutorial.phase.zero.PairLaterFragment

class TutorialActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "TutorialActivity"
    }

    private lateinit var binding: ActivityTutorialBinding
    private var currentFragment: TutorialFragment? = null
    private lateinit var mSectionsPagerAdapter: SectionsPagerAdapter
    private var prevPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tutorial)

        // Configuration des clics sur les boutons de l'UI
        binding.helpImageView.setOnClickListener { openOriiHelpWebsite() }
        binding.menuTextView.setOnClickListener { navigateToMainMenu() }
        binding.backTextView.setOnClickListener { navigateToBack() }
        binding.nextTextView.setOnClickListener { navigateToNext() }
        binding.tutorialMaskLayout.setOnClickListener { showMaskView(false) }

        // Configuration du ViewPager avec son adapter
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        binding.container.adapter = mSectionsPagerAdapter

        binding.container.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageScrollStateChanged(state: Int) {
                Log.d(TAG, state.toString())
            }
            override fun onPageSelected(position: Int) {
                Log.d(TAG, "Selected: $position")
                if (prevPosition != position) {
                    onPageChanged(position)
                    currentFragment?.onPageUnselected()
                }
                val tag = "android:switcher:${R.id.container}:${binding.container.currentItem}"
                val fragment = supportFragmentManager.findFragmentByTag(tag) as? TutorialFragment
                    ?: throw TypeCastException("null cannot be cast to non-null type TutorialFragment")
                currentFragment = fragment
                currentFragment?.onPageSelected()
                prevPosition = position
            }
        })
        onPageChanged(0)
    }

    fun openOriiHelpWebsite() {
        val intent = Intent(this, WebsiteActivity::class.java).apply {
            putExtra("websiteUrl", getString(R.string.question_support_orii_website_url))
        }
        startActivity(intent)
    }

    fun navigateToInitial() {
        Log.d(TAG, "navigateToInitial")
        binding.container.setCurrentItem(2, true)
    }

    fun navigateToNext() {
        Log.d(TAG, "navigateToNext")
        binding.container.setCurrentItem(binding.container.currentItem + 1, true)
    }

    fun navigateToBack() {
        Log.d(TAG, "navigateToBack")
        binding.container.setCurrentItem(binding.container.currentItem - 1, true)
    }

    fun navigateToMainMenu() {
        binding.container.setCurrentItem(5, true)
    }

    fun navigateToFindTheFit() {
        binding.container.setCurrentItem(6, true)
    }

    fun navigateToFindTheSweetSpot() {
        binding.container.setCurrentItem(10, true)
    }

    fun navigateToMessageReadout() {
        binding.container.setCurrentItem(13, true)
    }

    fun navigateToVoiceAssistant() {
        binding.container.setCurrentItem(14, true)
    }

    fun navigateToGesture() {
        binding.container.setCurrentItem(15, true)
    }

    fun navigateToGesturePage(position: Int) {
        when (position) {
            0 -> binding.container.setCurrentItem(15, true)
            1 -> binding.container.setCurrentItem(16, true)
            2 -> binding.container.setCurrentItem(17, true)
            3 -> binding.container.setCurrentItem(18, true)
        }
    }

    fun onPageChanged(position: Int) {
        when (position) {
            1, 2 -> {
                showHelpButton(false)
                showMenuButton(false)
                showBackButton(false)
                showNextButton(false)
                showCirclesView(false, null)
            }
            3, 4, 5 -> {
                showHelpButton(true)
                showMenuButton(false)
                showBackButton(false)
                showNextButton(false)
                showCirclesView(false, null)
            }
            6 -> {
                showHelpButton(true)
                showMenuButton(true)
                showBackButton(true)
                showNextButton(true)
                showMaskView(true)
                showCirclesView(true, arrayOf(2, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0))
            }
            7 -> {
                showHelpButton(true)
                showMenuButton(true)
                showBackButton(true)
                showNextButton(true)
                showCirclesView(true, arrayOf(1, 2, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0))
            }
            8 -> {
                showHelpButton(true)
                showMenuButton(true)
                showBackButton(true)
                showNextButton(true)
                showCirclesView(true, arrayOf(1, 1, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0))
            }
            9 -> {
                showHelpButton(true)
                showMenuButton(true)
                showBackButton(true)
                showNextButton(true)
                showCirclesView(true, arrayOf(1, 1, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0))
            }
            10 -> {
                showHelpButton(true)
                showMenuButton(true)
                showBackButton(true)
                showNextButton(true)
                showCirclesView(true, arrayOf(0, 0, 0, 0, 2, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0))
            }
            11 -> {
                showHelpButton(true)
                showMenuButton(true)
                showBackButton(true)
                showNextButton(true)
                showCirclesView(true, arrayOf(0, 0, 0, 0, 1, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0))
            }
            12 -> {
                showHelpButton(true)
                showMenuButton(true)
                showBackButton(true)
                showNextButton(true)
                showCirclesView(true, arrayOf(0, 0, 0, 0, 1, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0))
            }
            13 -> {
                showHelpButton(true)
                showMenuButton(true)
                showBackButton(true)
                showNextButton(true)
                showCirclesView(true, arrayOf(0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0))
            }
            14 -> {
                showHelpButton(true)
                showMenuButton(true)
                showBackButton(true)
                showNextButton(true)
                showCirclesView(true, arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0))
            }
            15 -> {
                showHelpButton(true)
                showMenuButton(true)
                showBackButton(true)
                showNextButton(true)
                showCirclesView(true, arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 1, 1, 1, 1, 1, 1))
            }
            16 -> {
                showHelpButton(true)
                showMenuButton(true)
                showBackButton(true)
                showNextButton(true)
                showCirclesView(true, arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 1, 1, 1, 1, 1))
            }
            17 -> {
                showHelpButton(true)
                showMenuButton(true)
                showBackButton(true)
                showNextButton(true)
                showCirclesView(true, arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 2, 1, 1, 1, 1))
            }
            18 -> {
                showHelpButton(true)
                showMenuButton(true)
                showBackButton(true)
                showNextButton(true)
                showCirclesView(true, arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 2, 1, 1, 1))
            }
            19 -> {
                showHelpButton(true)
                showMenuButton(true)
                showBackButton(true)
                showNextButton(true)
                showCirclesView(true, arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 1, 1))
            }
            20 -> {
                showHelpButton(true)
                showMenuButton(true)
                showBackButton(true)
                showNextButton(true)
                showCirclesView(true, arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 1))
            }
            21 -> {
                showHelpButton(true)
                showMenuButton(true)
                showBackButton(true)
                showNextButton(true)
                showCirclesView(true, arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2))
            }
            22 -> {
                showHelpButton(false)
                showMenuButton(false)
                showBackButton(false)
                showNextButton(false)
                showCirclesView(false, null)
            }
            else -> {
                showHelpButton(false)
                showMenuButton(false)
                showBackButton(false)
                showNextButton(false)
                showCirclesView(false, null)
            }
        }
    }

    private fun showHelpButton(isShow: Boolean) {
        binding.showHelpButton = isShow
    }

    private fun showMenuButton(isShow: Boolean) {
        binding.showMenuButton = isShow
    }

    private fun showBackButton(isShow: Boolean) {
        binding.showBackButton = isShow
    }

    private fun showNextButton(isShow: Boolean) {
        binding.showNextButton = isShow
    }

    fun showMaskView(isShow: Boolean) {
        binding.showMaskView = isShow
    }

    private fun showCirclesView(isShow: Boolean, circlesColor: Array<Int>?) {
        binding.showCircleView = isShow
        if (isShow && circlesColor != null) {
            circlesColor.forEachIndexed { index, color ->
                when (index) {
                    0 -> setCircleView(findViewById(R.id.circle_0_image_view) as ImageView, color)
                    1 -> setCircleView(findViewById(R.id.circle_1_image_view) as ImageView, color)
                    2 -> setCircleView(findViewById(R.id.circle_2_image_view) as ImageView, color)
                    3 -> setCircleView(findViewById(R.id.circle_3_image_view) as ImageView, color)
                    4 -> setCircleView(findViewById(R.id.circle_4_image_view) as ImageView, color)
                    5 -> setCircleView(findViewById(R.id.circle_5_image_view) as ImageView, color)
                    6 -> setCircleView(findViewById(R.id.circle_6_image_view) as ImageView, color)
                    7 -> setCircleView(findViewById(R.id.circle_7_image_view) as ImageView, color)
                    8 -> setCircleView(findViewById(R.id.circle_8_image_view) as ImageView, color)
                    9 -> setCircleView(findViewById(R.id.circle_9_image_view) as ImageView, color)
                    10 -> setCircleView(findViewById(R.id.circle_10_image_view) as ImageView, color)
                    11 -> setCircleView(findViewById(R.id.circle_11_image_view) as ImageView, color)
                    12 -> setCircleView(findViewById(R.id.circle_12_image_view) as ImageView, color)
                    13 -> setCircleView(findViewById(R.id.circle_13_image_view) as ImageView, color)
                    14 -> setCircleView(findViewById(R.id.circle_14_image_view) as ImageView, color)
                    15 -> setCircleView(findViewById(R.id.circle_15_image_view) as ImageView, color)
                }
            }
        }
    }

    private fun setCircleView(circleView: ImageView, color: Int) {
        when (color) {
            0 -> circleView.setImageDrawable(getDrawable(R.drawable.progress_circle_00))
            1 -> circleView.setImageDrawable(getDrawable(R.drawable.progress_circle_01))
            2 -> circleView.setImageDrawable(getDrawable(R.drawable.progress_circle_02))
        }
    }

    fun exitTutorial() {
        if (intent.getBooleanExtra("exit_with_clear_task", false)) {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
        } else {
            finish()
        }
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getCount(): Int = 23

        override fun getItem(position: Int): Fragment {
            return when (position) {
                1 -> PairLaterFragment.newInstance()
                2 -> InitialFragment.newInstance()
                3 -> TurnOnFragment.newInstance()
                4 -> ConnectionFragment.newInstance()
                5 -> MainMenuFragment.newInstance()
                6 -> UnscrewFragment.newInstance()
                7 -> UnlockFragment.newInstance()
                8 -> RightFitFragment.newInstance()
                9 -> ScrewFragment.newInstance()
                10 -> CloseEarFragment.newInstance()
                11 -> UsingOriiFragment.newInstance()
                12 -> RotateFragment.newInstance()
                13 -> ReadoutFragment.newInstance()
                14 -> CallVaFragment.newInstance()
                15 -> GestureAnglesFragment.newInstance()
                16 -> GestureMessageReadoutFragment.newInstance()
                17 -> GestureVoiceAssistantFragment.newInstance()
                18 -> GesturePlayPauseMusicFragment.newInstance()
                19 -> GestureSkipTrackFragment.newInstance()
                20 -> GestureCustomCommand1Fragment.newInstance()
                21 -> GestureCustomCommand2Fragment.newInstance()
                22 -> CompleteFragment.newInstance()
                else -> OpeningFragment.newInstance()
            }
        }
    }
}
