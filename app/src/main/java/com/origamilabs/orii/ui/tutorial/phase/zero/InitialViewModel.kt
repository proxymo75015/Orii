package com.origamilabs.orii.ui.tutorial.phase.zero

import androidx.lifecycle.ViewModel
import com.origamilabs.orii.manager.AppManager

class InitialViewModel : ViewModel() {
    fun getUsername(): String {
        val currentUser = AppManager.instance.currentUser
            ?: throw NullPointerException("Current user is null")
        return currentUser.name
    }
}
