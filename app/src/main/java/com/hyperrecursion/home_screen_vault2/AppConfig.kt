package com.hyperrecursion.home_screen_vault2

import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    var highFreqScanIntervalSecs: Int = 60,
    var lowFreqScanIntervalSecs: Int = 60 * 60,

    // vault path
    var vaultPath: String = "vault",
    var enableDebugLine: Boolean = false,
    var widthCorrection: Double = 1.0,
)