package com.hyperrecursion.home_screen_vault2.widget

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text

class WidgetReceiver2 : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = Widget2()
}

class Widget2 : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                ExampleContent2()
            }
        }
    }
}

// Example Content For learning Glance
@Composable
fun ExampleContent2(
    onOpen: () -> Unit = {}
) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val width: Dp = LocalSize.current.width
        val height: Dp = LocalSize.current.height
        Log.d("ExampleContent2", "width: $width, height: $height")
        Text(text = "aaa")
    }
}