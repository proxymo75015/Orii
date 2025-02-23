package com.origamilabs.orii.models

import com.google.gson.annotations.SerializedName

/**
 * Représente les contenus localisés pour une langue.
 *
 * @property common Contenu commun à tous les locales, modifiable si nécessaire.
 * @property en Contenu en anglais.
 * @property fr Contenu en français.
 * @property ja Contenu en japonais.
 * @property zh Contenu en chinois.
 */
data class Language(
    @SerializedName("common")
    var common: Content,
    @SerializedName("en")
    val en: Content,
    @SerializedName("fr")
    val fr: Content,
    @SerializedName("ja")
    val ja: Content,
    @SerializedName("zh")
    val zh: Content
)
