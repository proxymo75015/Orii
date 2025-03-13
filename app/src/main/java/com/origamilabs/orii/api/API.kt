package com.origamilabs.orii.api

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.util.Log
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.origamilabs.orii.R
import com.origamilabs.orii.api.request.OriiRequest
import io.fabric.sdk.android.services.common.AbstractSpiCall
import java.io.File

/**
 * Objet API regroupant l'ensemble des appels aux endpoints du serveur.
 */
object API {
    const val TAG = "API"

    /**
     * Interface pour la gestion des réponses aux requêtes API.
     */
    interface ResponseListener {
        fun onSuccess(response: JsonObject)
        fun onError(errorMessage: String)
    }

    /**
     * Effectue une requête de login avec email et mot de passe.
     */
    fun login(email: String, password: String, uuid: String, listener: ResponseListener) {
        val jsonObject = JsonObject().apply {
            addProperty("email", email)
            addProperty("password", password)
            addProperty(SharedPreferences.UUID, uuid)
        }
        VolleyManager.INSTANCE.addRequest(
            OriiRequest(
                method = 1,
                endpoint = "/login",
                json = jsonObject,
                listener = Response.Listener { response ->
                    val status: JsonElement = response.get("status")
                    if (status.asBoolean) {
                        listener.onSuccess(response)
                    } else {
                        val message = response.get("message").asString
                        listener.onError(message)
                    }
                },
                errorListener = Response.ErrorListener { error: VolleyError ->
                    val msg = error.message ?: "error message is null"
                    listener.onError(msg)
                }
            )
        )
    }

    /**
     * Effectue une requête de login avec vérification via un code de vérification.
     */
    fun loginWithVerify(userId: String, verifyCode: String, uuid: String, listener: ResponseListener) {
        val jsonObject = JsonObject().apply {
            addProperty("user_id", userId)
            addProperty("verify_code", verifyCode)
            addProperty(SharedPreferences.UUID, uuid)
        }
        VolleyManager.INSTANCE.addRequest(
            OriiRequest(
                method = 1,
                endpoint = "/login/with/verify",
                json = jsonObject,
                listener = Response.Listener { response ->
                    val status = response.get("status")
                    if (status.asBoolean) {
                        listener.onSuccess(response)
                    } else {
                        val message = response.get("message").asString
                        listener.onError(message)
                    }
                },
                errorListener = Response.ErrorListener { error ->
                    val msg = error.message ?: "error message is null"
                    listener.onError(msg)
                }
            )
        )
    }

    /**
     * Effectue une requête de login via Google.
     */
    fun googleLogin(email: String, googleId: String, googleIdToken: String, uuid: String, listener: ResponseListener) {
        val jsonObject = JsonObject().apply {
            addProperty("email", email)
            addProperty("google_id", googleId)
            addProperty("google_id_token", googleIdToken)
            addProperty(SharedPreferences.UUID, uuid)
        }
        VolleyManager.INSTANCE.addRequest(
            OriiRequest(
                method = 1,
                endpoint = "/google/login",
                json = jsonObject,
                listener = Response.Listener { response ->
                    val status = response.get("status")
                    if (status.asBoolean) {
                        listener.onSuccess(response)
                    } else {
                        val message = response.get("message").asString
                        listener.onError(message)
                    }
                },
                errorListener = Response.ErrorListener { error ->
                    val msg = error.message ?: "error message is null"
                    listener.onError(msg)
                }
            )
        )
    }

    /**
     * Effectue une requête de login via Facebook.
     */
    fun facebookLogin(email: String, facebookId: String, facebookIdToken: String, uuid: String, listener: ResponseListener) {
        val jsonObject = JsonObject().apply {
            addProperty("email", email)
            addProperty("facebook_id", facebookId)
            addProperty("facebook_id_token", facebookIdToken)
            addProperty(SharedPreferences.UUID, uuid)
        }
        VolleyManager.INSTANCE.addRequest(
            OriiRequest(
                method = 1,
                endpoint = "/facebook/login",
                json = jsonObject,
                listener = Response.Listener { response ->
                    val status = response.get("status")
                    if (status.asBoolean) {
                        listener.onSuccess(response)
                    } else {
                        val message = response.get("message").asString
                        listener.onError(message)
                    }
                },
                errorListener = Response.ErrorListener { error ->
                    val msg = error.message ?: "error message is null"
                    listener.onError(msg)
                }
            )
        )
    }

    /**
     * Effectue une requête d'inscription.
     */
    fun register(email: String, password: String, uuid: String, listener: ResponseListener) {
        val jsonObject = JsonObject().apply {
            addProperty("email", email)
            addProperty("password", password)
            addProperty(SharedPreferences.UUID, uuid)
        }
        VolleyManager.INSTANCE.addRequest(
            OriiRequest(
                method = 1,
                endpoint = "/register",
                json = jsonObject,
                listener = Response.Listener { response ->
                    val status = response.get("status")
                    if (status.asBoolean) {
                        listener.onSuccess(response)
                    } else {
                        val message = response.get("message").asString
                        listener.onError(message)
                    }
                },
                errorListener = Response.ErrorListener { error ->
                    listener.onError(error.message ?: "error message is null")
                }
            )
        )
    }

    /**
     * Envoie un feedback utilisateur.
     */
    fun postFeedback(
        token: String,
        userId: String,
        feedbackMsg: String,
        uuid: String,
        appVersion: String,
        listener: ResponseListener
    ) {
        val jsonObject = JsonObject().apply {
            addProperty("token", token)
            addProperty("user_id", userId)
            addProperty("feedback_message", feedbackMsg)
            addProperty(SharedPreferences.UUID, uuid)
            addProperty("device_type", AbstractSpiCall.ANDROID_CLIENT_TYPE)
            addProperty("app_version", appVersion)
        }
        VolleyManager.INSTANCE.addRequest(
            OriiRequest(
                method = 1,
                endpoint = "/post/feedback",
                json = jsonObject,
                listener = Response.Listener { response ->
                    val status = response.get("status")
                    if (status.asBoolean) {
                        listener.onSuccess(response)
                    } else {
                        val message = response.get("message").asString
                        listener.onError(message)
                    }
                },
                errorListener = Response.ErrorListener { error ->
                    val msg = error.message ?: "error message is null"
                    listener.onError(msg)
                }
            )
        )
    }

    /**
     * Déclenche une requête de réinitialisation de mot de passe.
     */
    fun forgotPassword(email: String, listener: ResponseListener) {
        val jsonObject = JsonObject().apply {
            addProperty("email", email)
        }
        VolleyManager.INSTANCE.addRequest(
            OriiRequest(
                method = 1,
                endpoint = "/forgot/password",
                json = jsonObject,
                listener = Response.Listener { response ->
                    val status = response.get("status")
                    if (status.asBoolean) {
                        listener.onSuccess(response)
                    } else {
                        val message = response.get("message").asString
                        listener.onError(message)
                    }
                },
                errorListener = Response.ErrorListener { error ->
                    val msg = error.message ?: "error message is null"
                    listener.onError(msg)
                }
            )
        )
    }

    /**
     * Vérifie la version du firmware sur l'appareil.
     */
    fun checkFirmwareVersion(version: Int, token: String, listener: ResponseListener) {
        val jsonObject = JsonObject().apply {
            addProperty("firmware_version", version)
            if (token.isNotEmpty()) {
                addProperty("token", token)
            }
        }
        VolleyManager.INSTANCE.addRequest(
            OriiRequest(
                method = 1,
                endpoint = "/check/firmware/android/version",
                json = jsonObject,
                listener = Response.Listener { response ->
                    val status = response.get("status")
                    if (status.asBoolean) {
                        listener.onSuccess(response)
                    } else {
                        val message = response.get("message").asString
                        listener.onError(message)
                    }
                },
                errorListener = Response.ErrorListener { error ->
                    val msg = error.message ?: "error message is null"
                    listener.onError(msg)
                }
            )
        )
    }

    /**
     * Télécharge le firmware depuis l'URL indiquée.
     *
     * @return l'ID de la requête de téléchargement.
     */
    fun downloadFirmware(context: Context, version: Int, url: String, description: String): Long {
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle(context.getString(R.string.app_name))
            setDescription(description)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            setDestinationInExternalFilesDir(context, "update", "v$version.bin")
        }
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return downloadManager.enqueue(request)
    }

    /**
     * Télécharge les informations de version de l'application.
     *
     * @return l'ID de la requête de téléchargement.
     */
    fun downloadAppVersionInformation(context: Context, url: String, description: String): Long {
        val externalFilesDir = context.getExternalFilesDir("update")
        val file = File(externalFilesDir?.absolutePath, "ORII.json")
        if (file.exists()) {
            file.delete()
        }
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle("ORII")
            setDescription(description)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            setDestinationInExternalFilesDir(context, "update", "ORII.json")
        }
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return downloadManager.enqueue(request)
    }

    /**
     * Envoie le compteur de l'assistant vice.
     */
    fun postViceAssistantCounter(
        userId: String,
        userToken: String,
        uuid: String,
        vaCounterList: JsonArray,
        listener: ResponseListener
    ) {
        val jsonObject = JsonObject().apply {
            addProperty("token", userToken)
            addProperty("user_id", userId)
            addProperty(SharedPreferences.UUID, uuid)
            add("datas", vaCounterList)
        }
        VolleyManager.INSTANCE.addRequest(
            OriiRequest(
                method = 1,
                endpoint = "/post/va",
                json = jsonObject,
                listener = Response.Listener { response ->
                    val status = response.get("status")
                    if (status.asBoolean) {
                        listener.onSuccess(response)
                    } else {
                        val message = response.get("message").asString
                        listener.onError(message)
                    }
                },
                errorListener = Response.ErrorListener { error ->
                    val msg = error.message ?: "error message is null pointer"
                    listener.onError(msg)
                }
            )
        )
    }

    /**
     * Déclenche une URI de webhook.
     */
    fun callWebHookTriggerUri(url: String) {
        VolleyManager.INSTANCE.addStringRequest(
            StringRequest(
                url,
                Response.Listener { response ->
                    Log.d(TAG, response)
                },
                Response.ErrorListener { /* Erreur ignorée */ }
            )
        )
    }
}
