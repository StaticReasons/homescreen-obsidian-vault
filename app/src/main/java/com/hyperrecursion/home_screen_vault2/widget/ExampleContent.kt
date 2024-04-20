package com.hyperrecursion.home_screen_vault2.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.Text
import com.hyperrecursion.home_screen_vault2.AppConfigActivity


// Example Content For learning Glance
@Composable
fun ExampleContent(
    onOpen: () -> Unit = {}
) {
    Column(
        modifier = GlanceModifier.fillMaxHeight(),
//        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "aaa", modifier = GlanceModifier.padding(12.dp))
        }

        Row(
            modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = GlanceModifier.defaultWeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    text = "bbb",
                    onClick = actionStartActivity<AppConfigActivity>()
                )
            }
            Column(
                modifier = GlanceModifier.defaultWeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    text = "ccc",
                    onClick = actionStartActivity<AppConfigActivity>()
                )
            }
        }
    }
}