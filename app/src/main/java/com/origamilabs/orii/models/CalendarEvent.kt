package com.origamilabs.orii.models

data class CalendarEvent(
    val event: String,
    val beginTime: String,
    val endTime: String,
    val allDay: Boolean
)
