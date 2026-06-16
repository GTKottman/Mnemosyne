package com.gtnoo.mnemosyne

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gtnoo.mnemosyne.presentation.settings.SettingsViewModel
import com.gtnoo.mnemosyne.ui.navigation.MnemosyneNavGraph
import com.gtnoo.mnemosyne.ui.navigation.Screen
import com.gtnoo.mnemosyne.ui.theme.MnemosyneTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MnemosyneTheme {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot(settingsViewModel: SettingsViewModel = hiltViewModel()) {
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val startDestination = if (settings.onboardingComplete) Screen.Home.route else Screen.Onboarding.route
    MnemosyneNavGraph(startDestination = startDestination)
}
