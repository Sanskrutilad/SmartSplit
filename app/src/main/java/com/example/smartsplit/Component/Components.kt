package com.example.smartsplit.data

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.Color




val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val DARK_MODE_KEY = stringPreferencesKey("dark_mode")
    }

    val darkModeFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: "Automatic"
        }

    suspend fun setDarkMode(option: String) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = option
        }
    }

    suspend fun getDarkMode(): String {
        return context.dataStore.data.map { preferences ->
            preferences[DARK_MODE_KEY] ?: "Automatic"
        }.first()
    }
}

@HiltViewModel
class DarkModeViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    val darkModeFlow = dataStoreManager.darkModeFlow
    val darkModeLiveData = darkModeFlow.asLiveData()

    fun setDarkMode(option: String) {
        viewModelScope.launch {
            dataStoreManager.setDarkMode(option)
        }
    }

    suspend fun getDarkMode(): String {
        return dataStoreManager.getDarkMode()
    }
}

@Composable
fun isDarkModeEnabled(): Boolean {
    val viewModel: DarkModeViewModel = hiltViewModel()
    val darkModeOption by viewModel.darkModeLiveData.observeAsState("Automatic")

    return when (darkModeOption) {
        "On" -> true
        "Off" -> false
        "Automatic" -> isSystemInDarkTheme()
        else -> false
    }
}

@Composable
fun BottomNavBar(navController: NavController, currentRoute: String, isDark: Boolean) {
    // Define colors internally
    val navBarColor = if (isDark) Color(0xFF121212) else Color.White
    val textColor = if (isDark) Color(0xFFB0B0B0) else Color.Black
    val accentColor = Color(0xFF1E88E5) // Blue

    NavigationBar(containerColor = navBarColor) {
        NavigationBarItem(
            selected = currentRoute == "group",
            onClick = { navController.navigate("group") },
            icon = {
                Icon(
                    Icons.Default.Group,
                    contentDescription = "Groups",
                    tint = if (currentRoute == "group") accentColor else textColor
                )
            },
            label = {
                Text(
                    "Groups",
                    color = if (currentRoute == "group") accentColor else textColor
                )
            }
        )
        NavigationBarItem(
            selected = currentRoute == "friends",
            onClick = { navController.navigate("friends") },
            icon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Friends",
                    tint = if (currentRoute == "friends") accentColor else textColor
                )
            },
            label = {
                Text(
                    "Friends",
                    color = if (currentRoute == "friends") accentColor else textColor
                )
            }
        )
        NavigationBarItem(
            selected = currentRoute == "history",
            onClick = { navController.navigate("history") },
            icon = {
                Icon(
                    Icons.Default.List,
                    contentDescription = "History",
                    tint = if (currentRoute == "history") accentColor else textColor
                )
            },
            label = {
                Text(
                    "History",
                    color = if (currentRoute == "history") accentColor else textColor
                )
            }
        )
        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = { navController.navigate("profile") },
            icon = {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = "Account",
                    tint = if (currentRoute == "profile") accentColor else textColor
                )
            },
            label = {
                Text(
                    "Account",
                    color = if (currentRoute == "profile") accentColor else textColor
                )
            }
        )
        NavigationBarItem(
            selected = currentRoute == "grocery",
            onClick = { navController.navigate("grocery") },
            icon = {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = "Grocery",
                    tint = if (currentRoute == "grocery") accentColor else textColor
                )
            },
            label = {
                Text(
                    "Grocery",
                    color = if (currentRoute == "grocery") accentColor else textColor
                )
            }
        )
    }
}


