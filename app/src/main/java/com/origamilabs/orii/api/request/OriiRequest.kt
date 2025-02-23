package com.origamilabs.orii.api.request

import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.HttpHeaderParser
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.origamilabs.orii.api.Config
import com.origamilabs.orii.db.SharedPreferences
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.HashMap

/**
 * Requête personnalisée pour l'API ORii.
 *
 * Cette classe étend [Request] de Volley pour envoyer des requêtes JSON.
 * Elle fournit plusieurs constructeurs pour accepter soit un objet [JsonObject],
 * soit une chaîne JSON, et configure une politique de réessai fixe.
 */
class OriiRequest : Request<JsonObject> {

    private var mListener: Response.Listener<JsonObject>? = null
    private var mRequestBody: String? = null

    companion object {
        private const val PROTOCOL_CHARSET = "utf-8"
        private val PROTOCOL_CONTENT_TYPE: String = "application/json; charset=$PROTOCOL_CHARSET"
        private const val TAG = "OriiRequest"
    }

    /**
     * Constructeur principal.
     *
     * @param method Méthode HTTP (GET, POST, …)
     * @param endPoint Chemin d'accès relatif (ajouté à l'URL de base)
     * @param errorListener Listener pour la gestion des erreurs
     */
    constructor(
        method: Int,
        endPoint: String,
        errorListener: Response.ErrorListener
    ) : super(method, Config.INSTANCE.serverUrl + endPoint, errorListener) {
        Log.d(TAG, endPoint)
    }

    /**
     * Constructeur secondaire acceptant un [JsonObject] comme corps de requête.
     *
     * @param jsonRequest Objet JSON représentant le corps de la requête
     * @param listener Listener pour la réponse réussie
     * @param errorListener Listener pour la gestion des erreurs
     */
    constructor(
        method: Int,
        endPoint: String,
        jsonRequest: JsonObject?,
        listener: Response.Listener<JsonObject>,
        errorListener: Response.ErrorListener
    ) : this(method, endPoint, errorListener) {
        mListener = listener
        mRequestBody = jsonRequest?.toString()
        Log.d(TAG, mRequestBody)
    }

    /**
     * Constructeur secondaire acceptant une chaîne JSON comme corps de requête.
     *
     * @param jsonRequest Chaîne JSON représentant le corps de la requête
     * @param listener Listener pour la réponse réussie
     * @param errorListener Listener pour la gestion des erreurs
     */
    constructor(
        method: Int,
        endPoint: String,
        jsonRequest: String,
        listener: Response.Listener<JsonObject>,
        errorListener: Response.ErrorListener
    ) : this(method, endPoint, errorListener) {
        mListener = listener
        mRequestBody = jsonRequest
    }

    override fun getHeaders(): Map<String, String> {
        val headers = HashMap<String, String>()
        headers["Parallaxhk"] = "ORii"
        return headers
    }

    override fun getBodyContentType(): String {
        return mRequestBody?.let { PROTOCOL_CONTENT_TYPE } ?: super.getBodyContentType()
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<JsonObject> {
        return try {
            val charset = Charset.forName(HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET))
            val jsonString = String(response.data, charset)
            Log.d(TAG, jsonString)
            val jsonElement = JsonParser().parse(jsonString)
            Response.success(jsonElement.asJsonObject, HttpHeaderParser.parseCacheHeaders(response))
        } catch (e: UnsupportedEncodingException) {
            Response.error(ParseError(e))
        }
    }

    override fun deliverResponse(response: JsonObject) {
        mListener?.onResponse(response)
    }

    override fun getBody(): ByteArray? {
        return try {
            if (params != null && params.isNotEmpty()) {
                super.getBody()
            } else {
                val requestBody = mRequestBody ?: throw NullPointerException("mRequestBody est null")
                requestBody.toByteArray(Charset.forName(PROTOCOL_CHARSET))
            }
        } catch (e: UnsupportedEncodingException) {
            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, PROTOCOL_CHARSET)
            null
        }
    }
}
