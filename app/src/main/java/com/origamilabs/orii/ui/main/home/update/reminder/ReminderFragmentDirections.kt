package com.origamilabs.orii.ui.main.home.update.reminder

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.origamilabs.orii.R

object ReminderFragmentDirections {
    fun actionReminderFragmentToBatteryAlertFragment(): NavDirections =
        ActionOnlyNavDirections(R.id.action_reminderFragment_to_batteryAlertFragment)

    fun actionReminderFragmentToUpdatingFragment(): NavDirections =
        ActionOnlyNavDirections(R.id.action_reminderFragment_to_updatingFragment)
}
