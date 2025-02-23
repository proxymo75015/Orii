package com.origamilabs.orii.api

/**
 * Objet de configuration pour l'API.
 *
 * Il définit la version de l'API ainsi que les points de terminaison pour le développement et la production.
 */
object Config {
    private const val API_VERSION: String = "1.0" // Définir ici la version de l'API souhaitée.
    const val DEV_END_POINT: String = "http://parallaxhk.ddns.net:3000/api/$API_VERSION"
    const val PRO_END_POINT: String = "https://admin.orii.co/api/$API_VERSION"

    fun getServerUrl(): String = PRO_END_POINT
}
