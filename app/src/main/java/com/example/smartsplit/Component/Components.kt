package com.example.smartsplit.screens.Component

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DarkModeViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = SettingsDataStore(application)

    private val _darkMode = MutableStateFlow(false)
    val darkMode: StateFlow<Boolean> = _darkMode

    init {
        dataStore.darkModeFlow
            .onEach { _darkMode.value = it }
            .launchIn(viewModelScope)
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            dataStore.setDarkMode(!_darkMode.value)
        }
    }
    fun resetDarkModeToDefault() {
        viewModelScope.launch {
            dataStore.setDarkMode(false)
        }
    }
}


private val Context.dataStore by preferencesDataStore("settings")


object SettingsKeys {
    val DARK_MODE = booleanPreferencesKey("dark_mode")
    val LANGUAGE = stringPreferencesKey("language")
}

class SettingsDataStore(private val context: Context) {

    val darkModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SettingsKeys.DARK_MODE] ?: false
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[SettingsKeys.DARK_MODE] = enabled }
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SettingsKeys.LANGUAGE] ?: "en"
    }

    suspend fun setLanguage(languageCode: String) {
        context.dataStore.edit { it[SettingsKeys.LANGUAGE] = languageCode }
    }
}
