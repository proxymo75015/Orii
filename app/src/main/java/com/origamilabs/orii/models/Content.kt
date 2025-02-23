package com.origamilabs.orii.models

import com.google.gson.annotations.SerializedName

data class Content(
    @SerializedName("version_bug_fixes")
    val bugFixes: String,
    @SerializedName("version_new_features")
    val newFeatures: String
)
