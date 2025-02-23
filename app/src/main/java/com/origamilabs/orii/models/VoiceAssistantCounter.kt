package com.origamilabs.orii.models

import com.google.gson.annotations.SerializedName

/**
 * Représente le compteur du Voice Assistant.
 *
 * @property times Le nombre de fois que le Voice Assistant a été utilisé.
 * @property date La date associée au compteur (au format souhaité, par exemple un timestamp ou un int représentant une date).
 * @property id Un identifiant unique, non inclus dans les méthodes générées automatiquement (equals, hashCode, copy).
 */
data class VoiceAssistantCounter(
    @SerializedName("va_times")
    var times: Int,
    @SerializedName("va_date")
    var date: Int
) {
    // Ce champ n'est pas inclus dans les méthodes générées automatiquement.
    var id: Int = 0
    // Si vous ne souhaitez pas que 'id' soit sérialisé, vous pouvez décommenter la ligne suivante :
    // @Transient var id: Int = 0
}
