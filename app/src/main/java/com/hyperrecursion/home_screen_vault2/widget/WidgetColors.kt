package com.hyperrecursion.home_screen_vault2.widget

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.glance.material3.ColorProviders
import androidx.glance.unit.ColorProvider

// Remember, use the Glance imports
// import androidx.glance.material3.ColorProviders

// Example Imports from your own app
// import com.example.myapp.ui.theme.DarkColors
// import com.example.myapp.ui.theme.LightColors

val widgetColors = ColorProviders(
    light = lightColorScheme(
        surface = Color(0xffffffff),
        background = Color(0x44ffffff),
        primary = Color(0x44ffffff),
        onPrimary = Color(0xff000000),
        outline = Color(0xff222222),
    ),
    dark = darkColorScheme(
        surface = Color(0xff080808),
        background = Color(0x44080808),
        primary = Color(0x44080808),
        onPrimary = Color(0xffffffff),
        outline = Color(0xffcccccc),
    )
)

val starColor = ColorProvider(
    color = Color(0xffffeb3b),
)