package com.origamilabs.orii.voice.commands

object DefaultCommand {
    fun execute(command: String, speak: (String) -> Unit) {
        speak("Votre assistant virtuel n'a plus accès à Internet. Seules les commandes vocales peuvent désormais être traitées")
    }
}
