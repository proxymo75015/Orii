package com.origamilabs.orii.ui.main.home.update.info

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.origamilabs.orii.R

object UpdateInfoFragmentDirections {
    fun actionUpdateInfoFragmentToReminderFragment(): NavDirections =
        ActionOnlyNavDirections(R.id.action_updateInfoFragment_to_reminderFragment)
}
