package com.farm.farmestar

import android.content.Context
import android.content.SharedPreferences

class Prefs (context: Context) {
    val PREFS_FILENAME = "com.farm.farmestar"
    val APP_VERSION = "app_version"
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0)

    var version: String?
        get() = prefs.getString(APP_VERSION, "0.0.0")
        set(value) = prefs.edit().putString(APP_VERSION, value).apply()
}