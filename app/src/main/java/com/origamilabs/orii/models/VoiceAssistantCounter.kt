package com.origamilabs.orii.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "va_counter")
data class VoiceAssistantCounter(

    @SerializedName("va_times")
    var times: Int,

    @SerializedName("va_date")
    var date: Int
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
