package com.origamilabs.orii.ui.main.settings.gesture

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.origamilabs.orii.R

/**
 * Activité qui présente un tutoriel pour le WebHook.
 *
 * Elle permet à l'utilisateur de revenir en arrière ou de lancer la lecture d'une vidéo YouTube.
 */
class WebHookTutorialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gesture_web_hook_tutorial)
        initListener()
    }

    private fun initListener() {
        // Bouton de retour : ferme l'activité.
        findViewById<TextView>(R.id.web_hook_tutorial_back_text_view)?.setOnClickListener {
            finish()
        }
        // Bouton pour jouer la vidéo tutoriel.
        findViewById<TextView>(R.id.web_hook_tutorial_play_video_text_view)?.setOnClickListener {
            watchYoutubeVideo()
        }
    }

    fun watchYoutubeVideo() {
        // Intent pour lancer l'application YouTube
        val youtubeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:QjZUtTrGd_4"))
        // Intent de repli pour ouvrir la vidéo dans un navigateur web
        val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=QjZUtTrGd_4&feature=youtu.be"))
        try {
            startActivity(youtubeIntent)
        } catch (e: ActivityNotFoundException) {
            startActivity(fallbackIntent)
        }
    }
}
