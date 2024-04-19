package com.hyperrecursion.home_screen_vault2

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class AppConfigViewModel(
    private val appConfigRepository: AppConfigRepository
) : ViewModel() {

    init {
        loadAppConfig()
    }

    var uiState by mutableStateOf(AppConfigUiState(initAvailableState = false))
        private set

    fun saveConfig(onSuccess: () -> Unit = {}, onError: (Exception) -> Unit = {}) {
        if (!uiState.isValid) {
            onError(Exception("Invalid config"))
            return;
        }
        viewModelScope.launch {
            appConfigRepository.saveAppConfig(uiState.toAppConfig()?: AppConfig())
            onSuccess()
        }
    }

    fun loadAppConfig() {
        viewModelScope.launch {
            val appConfig = appConfigRepository.getAppConfigFlow().first()
            uiState = AppConfigUiState.fromAppConfig(appConfig)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                // Used a pretty weird kind of method to get the dependency...
                // hope hilt could fix the bug of @HiltViewModel asap
                val myRepository = (this[APPLICATION_KEY] as App).myRepository
                AppConfigViewModel(
                    appConfigRepository = myRepository
                )
            }
        }
    }
}

class AppConfigUiState(
    initHighFreqScanIntervalSecs: Int = 60,
    initLowFreqScanIntervalSecs: Int = 12 * 60,
    initVaultPath: String = "vault",
    initWidthCorrection: Double = 1.0,
    initEnableDebugLine: Boolean = false,
    initAvailableState: Boolean = true
) {
    var textFieldHighFreqScanIntervalSecs by mutableStateOf(initHighFreqScanIntervalSecs.toString())
    var highFreqScanIntervalSecs: Int?
        get() = textFieldHighFreqScanIntervalSecs.toIntOrNull()?.takeIf { it >= MIN_HIGH_FREQ_SCAN_INTERVAL_SECS }
        private set(value) {
            textFieldHighFreqScanIntervalSecs = value.toString()
        }

    var textFieldLowFreqScanIntervalSecs by mutableStateOf(initLowFreqScanIntervalSecs.toString())
    var lowFreqScanIntervalSecs: Int?
        get() = textFieldLowFreqScanIntervalSecs.toIntOrNull()?.takeIf { it >= MIN_LOW_FREQ_SCAN_INTERVAL_SECS }
        private set(value) {
            textFieldLowFreqScanIntervalSecs = value.toString()
        }

    var vaultPath by mutableStateOf(initVaultPath)
    val vaultPathIsValid: Boolean get() = AppConfigRepository.checkPathExists(vaultPath)

    var textFieldWidthCorrection by mutableStateOf(initWidthCorrection.toString())
    var widthCorrection: Double?
        get() = textFieldWidthCorrection.toDoubleOrNull()?.takeIf { it > 0.0 }
        private set(value) {
            textFieldWidthCorrection = value.toString()
        }

    var enableDebugLine by mutableStateOf(initEnableDebugLine)

    var availableState by mutableStateOf(initAvailableState)

    val isValid: Boolean
        get() = highFreqScanIntervalSecs != null
                && lowFreqScanIntervalSecs != null
                && vaultPathIsValid

    fun toAppConfig(): AppConfig? =
        if (isValid) AppConfig(
            highFreqScanIntervalSecs = highFreqScanIntervalSecs ?: 60,
            lowFreqScanIntervalSecs = lowFreqScanIntervalSecs ?: (60 * 60),
            vaultPath = vaultPath,
            enableDebugLine = enableDebugLine
        ) else null

    companion object {
        const val MIN_HIGH_FREQ_SCAN_INTERVAL_SECS = 3
        const val MIN_LOW_FREQ_SCAN_INTERVAL_SECS = 6
        fun fromAppConfig(appConfig: AppConfig): AppConfigUiState{
            return AppConfigUiState().apply {
                highFreqScanIntervalSecs = appConfig.highFreqScanIntervalSecs
                lowFreqScanIntervalSecs = appConfig.lowFreqScanIntervalSecs
                vaultPath = appConfig.vaultPath
                enableDebugLine = appConfig.enableDebugLine
            }
        }
    }
}