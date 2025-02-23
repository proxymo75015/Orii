package com.origamilabs.orii.api

import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.origamilabs.orii.api.request.OriiRequest

/**
 * Singleton pour gérer la file de requêtes Volley.
 *
 * Avant utilisation, appelez [init] avec un contexte.
 * Vous pouvez ensuite ajouter des requêtes via [addRequest] ou [addStringRequest].
 */
object VolleyManager {
    private lateinit var applicationContext: Context
    private lateinit var requestQueue: RequestQueue

    /**
     * Initialise le VolleyManager avec le contexte de l'application.
     *
     * @param context Le contexte à utiliser pour créer la file de requêtes.
     */
    fun init(context: Context) {
        applicationContext = context
        requestQueue = Volley.newRequestQueue(context)
    }

    /**
     * Ajoute une requête personnalisée de type [OriiRequest] à la file de requêtes.
     *
     * La requête est configurée avec une politique de réessai fixe.
     *
     * @param request La requête à ajouter.
     */
    fun addRequest(request: OriiRequest) {
        request.retryPolicy = DefaultRetryPolicy(0, 1, 1.0f)
        requestQueue.add(request)
    }

    /**
     * Ajoute une requête Volley générique à la file de requêtes.
     *
     * La requête est configurée avec une politique de réessai fixe.
     *
     * @param request La requête à ajouter.
     */
    fun addRequest(request: Request<*>) {
        request.retryPolicy = DefaultRetryPolicy(0, 1, 1.0f)
        requestQueue.add(request)
    }

    /**
     * Ajoute une [StringRequest] à la file de requêtes.
     *
     * La requête est configurée avec une politique de réessai fixe.
     *
     * @param stringRequest La requête sous forme de chaîne de caractères.
     */
    fun addStringRequest(stringRequest: StringRequest) {
        stringRequest.retryPolicy = DefaultRetryPolicy(0, 1, 1.0f)
        requestQueue.add(stringRequest)
    }
}
