package com.agomes.myincome.util

import android.support.annotation.Nullable
import com.agomes.myincome.MyIncomeApplication

/**
 * Created by agomes on 4/8/18.
 */
object PreferenceUtil {

    // string
    fun writePreferenceValue(prefsKey: String, prefsValue: String) {
        MyIncomeApplication.getPreferences().edit().putString(prefsKey, prefsValue).apply()
    }

    // int
    fun writePreferenceValue(prefsKey: String, prefsValue: Int) {
        MyIncomeApplication.getPreferences().edit().putInt(prefsKey, prefsValue).apply()
    }

    // boolean
    fun writePreferenceValue(prefsKey: String, prefsValue: Boolean) {
        MyIncomeApplication.getPreferences().edit().putBoolean(prefsKey, prefsValue).apply()
    }

    // float
    fun writePreferenceValue(prefsKey: String, prefsValue: Float) {
        MyIncomeApplication.getPreferences().edit().putFloat(prefsKey, prefsValue).apply()
    }

    // string
    fun readPreferenceValue(prefsKey: String, @Nullable defaultValue: String): String {
        return MyIncomeApplication.getPreferences().getString(prefsKey, defaultValue)
    }

    // int
    fun readPreferenceValue(prefsKey: String, defaultValue: Int): Int {
        return MyIncomeApplication.getPreferences().getInt(prefsKey, defaultValue)
    }

    // boolean
    fun readPreferenceValue(prefsKey: String, defaultValue: Boolean): Boolean {
        return MyIncomeApplication.getPreferences().getBoolean(prefsKey, defaultValue)
    }

    // float
    fun readPreferenceValue(prefsKey: String, defaultValue: Float): Float {
        return MyIncomeApplication.getPreferences().getFloat(prefsKey, defaultValue)
    }

    fun deletePreference(prefKey: String) {
        MyIncomeApplication.getPreferences().edit().remove(prefKey).apply()
    }
}