/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codelab.android.datastore.data

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.datastore.core.DataStore
import com.codelab.android.database.UserPreferences
import com.codelab.android.database.UserPreferences.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import java.io.IOException

private const val USER_PREFERENCES_NAME = "user_preferences"
private const val SORT_ORDER_KEY = "sort_order"


/**
 * Class that handles saving and retrieving user preferences
 */
class UserPreferencesRepository constructor(
    private val userPreferencesStore: DataStore<UserPreferences>,
    context: Context
) {
    private val TAG = "UserPreferencesRepo"
    private val sharedPreferences =
        context.applicationContext.getSharedPreferences(USER_PREFERENCES_NAME, Context.MODE_PRIVATE)

    // Keep the sort order as a stream of changes
    private val _sortOrderFlow = MutableStateFlow(sortOrder)
    val sortOrderFlow: StateFlow<SortOrder> = _sortOrderFlow

    val userPreferencesFlow: Flow<UserPreferences> = userPreferencesStore.data.catch { exception ->
        if (exception is IOException) {
            Log.e(TAG, "Error reading sort order preference", exception)
            emit(UserPreferences.getDefaultInstance())
        } else {
            throw exception
        }
    }

    /**
     * Get the sort order. By default, sort order is None.
     */
    private val sortOrder: SortOrder
        get() {
            val order = sharedPreferences.getString(SORT_ORDER_KEY, SortOrder.NONE.name)
            return SortOrder.valueOf(order ?: SortOrder.NONE.name)
        }

    suspend fun enableSortByDeadline(enable: Boolean) {
        userPreferencesStore.updateData { preferences ->
            val currentOrder = preferences.sortOrder
            val newSortOrder =
                if (enable) {
                    if (currentOrder == SortOrder.BY_PRIORITY) {
                        SortOrder.BY_DEADLINE_AND_PRIORITY
                    } else {
                        SortOrder.BY_DEADLINE
                    }
                } else {
                    if (currentOrder == SortOrder.BY_DEADLINE_AND_PRIORITY) {
                        SortOrder.BY_PRIORITY
                    } else {
                        SortOrder.NONE
                    }
                }
            preferences.toBuilder().setSortOrder(newSortOrder).build()
        }
    }

    suspend fun enableSortByPriority(enable: Boolean) {
        userPreferencesStore.updateData { preferences ->
            val currentOrder = preferences.sortOrder
            val newSortOrder =
                if (enable) {
                    if (currentOrder == SortOrder.BY_DEADLINE) {
                        SortOrder.BY_DEADLINE_AND_PRIORITY
                    } else {
                        SortOrder.BY_PRIORITY
                    }
                } else {
                    if (currentOrder == SortOrder.BY_DEADLINE_AND_PRIORITY) {
                        SortOrder.BY_DEADLINE
                    } else {
                        SortOrder.NONE
                    }
                }
            preferences.toBuilder().setSortOrder(newSortOrder).build()
        }
    }

    suspend fun updateShowCompleted(show: Boolean) {
        userPreferencesStore.updateData { preferences ->
            preferences.toBuilder().setShowCompleted(show).build()
        }
    }
}
