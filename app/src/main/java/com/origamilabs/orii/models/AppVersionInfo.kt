package com.origamilabs.orii.models

import com.google.gson.annotations.SerializedName

data class AppVersionInfo(
    @SerializedName("app_name")
    val appName: String,
    @SerializedName("version_code")
    val versionCode: Int,
    @SerializedName("url")
    val url: String,
    @SerializedName("remark")
    val remark: String,
    @SerializedName("status")
    val status: Int,
    @SerializedName("language")
    val language: Language
)
