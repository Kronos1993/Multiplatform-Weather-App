package com.kronos.multiplatform.weatherapp.features.home.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.kronos.multiplatform.weatherapp.device.screen_config.DeviceScreenConfiguration
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavHostController,
    isDarkTheme: Boolean,
    deviceScreenConfiguration: DeviceScreenConfiguration
) {
    val viewModel = koinViewModel<AboutViewModel>()
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            containerColor = MaterialTheme.colorScheme.onPrimary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) { innerPadding ->

            val rootModifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Transparent)
                .consumeWindowInsets(WindowInsets.navigationBars)

            when (deviceScreenConfiguration) {
                DeviceScreenConfiguration.MOBILE_PORTRAIT -> {
                    Column(
                        modifier = rootModifier
                            .padding(8.dp)
                            .verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        AboutHeaderSection(
                            appVersion = viewModel.appVersion,
                            alignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth(),
                            darkTheme = isDarkTheme
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AboutInfoSection(
                            expectedIntents = viewModel.intents,
                            modifier = Modifier.fillMaxWidth(),
                            darkTheme = isDarkTheme
                        )
                    }
                }

                DeviceScreenConfiguration.TABLET_PORTRAIT,
                DeviceScreenConfiguration.TABLET_LANDSCAPE,
                DeviceScreenConfiguration.DESKTOP,
                DeviceScreenConfiguration.MOBILE_LANDSCAPE -> {
                    Row(
                        modifier = rootModifier
                            .windowInsetsPadding(WindowInsets.displayCutout)
                            .padding(horizontal = 32.dp, vertical = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                    ) {
                        AboutHeaderSection(
                            appVersion = viewModel.appVersion,
                            modifier = Modifier.weight(.5f),
                            darkTheme = isDarkTheme
                        )
                        AboutInfoSection(
                            expectedIntents = viewModel.intents,
                            darkTheme = isDarkTheme,
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(scrollState),
                        )
                    }
                }
            }
        }
    }
}
