package com.gtnoo.mnemosyne

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gtnoo.mnemosyne.notification.ImportantDateNotificationHelper
import com.gtnoo.mnemosyne.presentation.settings.SettingsViewModel
import com.gtnoo.mnemosyne.ui.navigation.MnemosyneNavGraph
import com.gtnoo.mnemosyne.ui.navigation.Screen
import com.gtnoo.mnemosyne.ui.theme.MnemosyneTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        fun createIntent(
            context: Context,
            personId: String? = null,
            openInteraction: Boolean = false
        ): Intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            personId?.let { putExtra(ImportantDateNotificationHelper.EXTRA_PERSON_ID, it) }
            if (openInteraction) putExtra(ImportantDateNotificationHelper.EXTRA_OPEN_INTERACTION, true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val personId = intent.getStringExtra(ImportantDateNotificationHelper.EXTRA_PERSON_ID)
        val openInteraction = intent.getBooleanExtra(ImportantDateNotificationHelper.EXTRA_OPEN_INTERACTION, false)
        setContent {
            MnemosyneTheme {
                AppRoot(
                    initialPersonId = personId,
                    initialOpenInteraction = openInteraction
                )
            }
        }
    }
}

@Composable
private fun AppRoot(
    initialPersonId: String? = null,
    initialOpenInteraction: Boolean = false,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val startDestination = if (settings.onboardingComplete) Screen.Home.route else Screen.Onboarding.route
    MnemosyneNavGraph(
        startDestination = startDestination,
        initialPersonId = initialPersonId,
        initialOpenInteraction = initialOpenInteraction
    )
}
