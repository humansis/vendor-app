package cz.quanti.android.vendor_app.utils.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import org.koin.core.KoinComponent
import org.koin.core.inject

object SharedPreferencesManager : ISharedPreferencesManager, KoinComponent {

    private const val PREF_NAME = "eaaci_attendace_logger_preferences"
    private val context: Context by inject()
    private var sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)!!

    @SuppressLint("CommitPrefEdits")
    private fun saveIntValue(key: String, value: Int) {
        sharedPreferences.edit()
            .putInt(key, value)
            .confirm()
    }

    @SuppressLint("CommitPrefEdits")
    private fun saveStringValue(key: String, value: String) {
        sharedPreferences.edit()
            .putString(key, value)
            .confirm()
    }

    @SuppressLint("CommitPrefEdits")
    private fun saveFloatValue(key: String, value: Float) {
        sharedPreferences.edit()
            .putFloat(key, value)
            .confirm()
    }

    @SuppressLint("CommitPrefEdits")
    private fun saveBoolValue(key: String, value: Boolean) {
        sharedPreferences.edit()
            .putBoolean(key, value)
            .confirm()
    }

    @SuppressLint("CommitPrefEdits")
    private fun saveLongValue(key: String, value: Long) {
        sharedPreferences.edit()
            .putLong(key, value)
            .confirm()
    }

    override fun saveValue(key: String, value: Any) {
        when (value) {
            is Int -> saveIntValue(key, value)
            is Boolean -> saveBoolValue(key, value)
            is String -> saveStringValue(key, value)
            is Float -> saveFloatValue(key, value)
            is Long -> saveLongValue(key, value)
        }
    }

    override fun getValue(key: String, defaultValue: Any): Any {
        return when (defaultValue) {
            is Int -> sharedPreferences.getInt(key, defaultValue)
            is Boolean -> sharedPreferences.getBoolean(key, defaultValue)
            is String -> sharedPreferences.getString(key, defaultValue)
            is Float -> sharedPreferences.getFloat(key, defaultValue)
            is Long -> sharedPreferences.getLong(key, defaultValue)
            else -> throw NotImplementedError()
        }
    }

    @SuppressLint("CommitPrefEdits")
    override fun clearAll() {
        sharedPreferences.edit().clear().confirm()
    }
}

private fun SharedPreferences.Editor.confirm() = run {
    this.apply()
    this.commit()
}
