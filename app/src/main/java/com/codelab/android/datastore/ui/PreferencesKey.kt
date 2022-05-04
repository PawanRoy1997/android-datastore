package com.codelab.android.datastore.ui

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesKey {
    val SHOW_COMPLETED = booleanPreferencesKey("show_completed")
    val SORT_ORDER = stringPreferencesKey("sort_order")
}