package com.origamilabs.orii.notification.models

/**
 * Collection de messages regroupés par expéditeur.
 */
class MessageCollection {

    /**
     * Liste des expéditeurs. Chaque expéditeur contient ses messages regroupés en créneaux horaires (TimeSlot).
     */
    var senders: ArrayList<Sender> = arrayListOf()
        private set

    /**
     * Ajoute un message à la collection.
     *
     * @param appName Le nom de l'application associée.
     * @param sender L'expéditeur du message.
     * @param message Le contenu du message.
     * @param timestamp L'horodatage du message.
     */
    fun add(appName: String, sender: String, message: String, timestamp: Int) {
        // Recherche d'un expéditeur existant
        val existingSender = senders.find { it.name == sender }
        if (existingSender != null) {
            existingSender.addMessage(message, timestamp, appName)
        } else {
            senders.add(Sender(sender, message, timestamp, appName))
        }
    }

    /**
     * Retourne le nombre d'expéditeurs dans la collection.
     */
    fun size(): Int = senders.size

    /**
     * Réinitialise la collection en vidant la liste des expéditeurs.
     */
    fun clear() {
        senders = arrayListOf()
    }

    /**
     * Représente un expéditeur regroupant des messages.
     *
     * @property name Le nom de l'expéditeur.
     * @property timestamp L'horodatage du premier message du premier TimeSlot (mis à jour lors de la suppression d'un TimeSlot).
     * @property timeSlots La liste des créneaux horaires regroupant les messages.
     */
    inner class Sender(
        var name: String,
        msg: String,
        timestamp: Int,
        appName: String
    ) {
        var timeSlots: ArrayList<TimeSlot> = arrayListOf()
            private set

        var timestamp: Int = timestamp
            private set

        init {
            addMessage(msg, timestamp, appName)
        }

        /**
         * Ajoute un message à cet expéditeur.
         *
         * Si le dernier créneau horaire (TimeSlot) peut accueillir le message (selon la condition temporelle),
         * le message y est ajouté. Sinon, un nouveau créneau est créé.
         *
         * @param msg Le contenu du message.
         * @param timestamp L'horodatage du message.
         * @param appName Le nom de l'application associée.
         */
        fun addMessage(msg: String, timestamp: Int, appName: String) {
            if (timeSlots.isNotEmpty() && timeSlots.last().shouldInThisTimeSlot(timestamp)) {
                timeSlots.last().addMessage(msg, timestamp, appName)
            } else {
                timeSlots.add(TimeSlot(msg, timestamp, appName))
            }
        }

        /**
         * Supprime le premier créneau horaire et met à jour l'horodatage si d'autres créneaux existent.
         */
        fun removeTimeSlot() {
            if (timeSlots.isNotEmpty()) {
                timeSlots.removeAt(0)
                if (timeSlots.isNotEmpty()) {
                    this.timestamp = timeSlots.first().messages.first().timestamp
                }
            }
        }
    }

    /**
     * Représente un créneau horaire regroupant plusieurs messag
