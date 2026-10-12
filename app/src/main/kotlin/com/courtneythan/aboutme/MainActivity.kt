package com.courtneythan.aboutme

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.courtneythan.aboutme.core.designsystem.theme.AboutMeTheme
import com.courtneythan.aboutme.feature.profile.ProfileRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Every screen starts with an ink header, so the status bar icons are light.
        // The bottom of the screen is cream, so the navigation bar icons are dark.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        setContent {
            AboutMeTheme {
                ProfileRoute()
            }
        }
    }
}
