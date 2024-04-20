package com.hyperrecursion.home_screen_vault2

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hyperrecursion.home_screen_vault2.scanner.ScanningWorker
import com.hyperrecursion.home_screen_vault2.ui.theme.Homescreenvault2Theme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AppConfigActivity : ComponentActivity() {

    @Inject
    lateinit var appConfigRepository: AppConfigRepository

    private fun startFolderPicker(callback: (Uri?) -> Unit) {
        val folderPickerLauncher = this.registerForActivityResult(
            ActivityResultContracts.OpenDocumentTree()
        ) { uri -> callback(uri) }
        folderPickerLauncher.launch(null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 30) {
            if (!Environment.isExternalStorageManager()) {
                val uri = Uri.parse("package:${packageName}")
                startActivity(
                    Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)
                )
            }
        }

        setContent {
            Homescreenvault2Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    AppConfigForm(
                        modifier = Modifier,
//                        toRegisterWorker = { registerVaultScannerWorker(it) }
                    )
                }
            }
        }
    }
}

//fun setupScanWork(context: Context) {
//    val uploadWorkRequest =
//        PeriodicWorkRequestBuilder<ScanningWorker>(1, TimeUnit.HOURS)
//            .build()
//    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
//        ScanningWorker.WORK_NAME,
//        WorkRequest.KeepResult.RETAIN_DATA,
//        uploadWorkRequest
//    )
//}

@Composable
fun AppConfigForm(
    modifier: Modifier = Modifier,
//    toRegisterWorker: (AppConfig) -> Unit = {},
    viewModel: AppConfigViewModel = viewModel(factory = AppConfigViewModel.Factory),
) {
    val coroutineScope = rememberCoroutineScope()
    val uiState = viewModel.uiState
    val context = LocalContext.current

    if (!uiState.availableState) {
        // display loading
        return Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
        ) {
            CircularProgressIndicator()
        }
    }

    Column(modifier = modifier.padding(16.dp)) {

        // High Frequency Scan Interval
        Text(
            text = "High Frequency Scan Interval (seconds, >= ${AppConfigUiState.MIN_HIGH_FREQ_SCAN_INTERVAL_SECS}s)",
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            label = { if (uiState.highFreqScanIntervalSecs == null) Text(text = "Invalid Config") },
            value = uiState.textFieldHighFreqScanIntervalSecs,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            onValueChange = {
                uiState.textFieldHighFreqScanIntervalSecs = it
            },
            isError = uiState.highFreqScanIntervalSecs == null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Low Frequency Scan Interval
        Text(
            text = "Low Frequency Scan Interval (seconds, >= ${AppConfigUiState.MIN_LOW_FREQ_SCAN_INTERVAL_SECS}s)",
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            label = { if (uiState.lowFreqScanIntervalSecs == null) Text(text = "Invalid Config") },
            value = uiState.textFieldLowFreqScanIntervalSecs,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            onValueChange = {
                uiState.textFieldLowFreqScanIntervalSecs = it
            },
            isError = uiState.lowFreqScanIntervalSecs == null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Vault Path
        Text(
            text = "Vault Path (relative to the public storage)",
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // Vault Path Text Field
            OutlinedTextField(
                label = { if (!uiState.vaultPathIsValid) Text(text = "Invalid Config") },
                value = uiState.vaultPath,
                onValueChange = {
                    uiState.vaultPath = it
                    Log.d("AppConfigForm", "vaultPath: $it")
                    Log.d("AppConfigForm", "appConfig: $uiState")
                },
                isError = !uiState.vaultPathIsValid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                singleLine = true, modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            val folderPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocumentTree()
            ) {
                //log
                Log.d("AppConfigForm", "folderPickerLauncher result: $it")
                it?.path?.split(":")?.get(1)?.let { it1 ->
                    uiState.vaultPath = it1
                    Log.d("AppConfigForm", "appConfig: $uiState")
                }
            }
            // Folder Picker Button
            IconButton(
                onClick = { folderPickerLauncher.launch(null) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    painterResource(id = R.drawable.outline_folder_24),
                    contentDescription = "Choose folder"
                )
            }
        }

        // Additional option: whether to enable a debug line
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "Enable a debug line, to show how long is the system provided width of the widget after correction",
                modifier = Modifier
//                    .padding(start = 8.dp)
                    .align(Alignment.CenterVertically)
                    .weight(1f),
                maxLines = 5
            )
            Spacer(modifier = Modifier.width(2.dp))
            Switch(
                checked = uiState.enableDebugLine,
                onCheckedChange = { uiState.enableDebugLine = it })
        }

        // Width Correction
        Text(
            text = "Width Correction Coefficient(> 0.0), in case the system provides a wrong widget width " +
                    "(i.e. landscape's size for portrait widget) --- then that width will be multiplied by this coefficient",
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            label = { if (uiState.widthCorrection == null) Text(text = "Invalid Config") },
            value = uiState.textFieldWidthCorrection,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            onValueChange = {
                uiState.textFieldWidthCorrection = it
            },
            isError = uiState.widthCorrection == null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Save Button
        Button(
            enabled = uiState.isValid,
            onClick = {
                coroutineScope.launch {
                    viewModel.saveConfig(
                        onSuccess = {
                            ScanningWorker.updateWork(
                                uiState.highFreqScanIntervalSecs?.toLong() ?: 60, context = context
                            )
                            Toast.makeText(
                                context,
                                "App configuration saved successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }, modifier = Modifier.align(Alignment.End)
        ) {
            Text(text = "Save")
        }

        val annotatedLinkString: AnnotatedString = buildAnnotatedString {

            val str = "Credits: Icons partly provided by Icons8"
            val startIndex = str.indexOf("Icons8")
            val endIndex = startIndex + 6
            append(str)
            addStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp,
                ),
                start = 0, end = str.length
            )
            addStyle(
                style = SpanStyle(
                    color = Color(0xff64B5F6),
                    fontSize = 18.sp,
                    textDecoration = TextDecoration.Underline
                ),
                start = startIndex, end = endIndex
            )

            // attach a string annotation that stores a URL to the text "link"
            addStringAnnotation(
                tag = "URL",
                annotation = "https://icons8.com/",
                start = startIndex,
                end = endIndex
            )

        }

// UriHandler parse and opens URI inside AnnotatedString Item in Browse
        val uriHandler = LocalUriHandler.current

// Clickable text returns position of text that is clicked in onClick callback
        ClickableText(
            modifier = modifier
                .padding(16.dp)
                .fillMaxWidth(),
            text = annotatedLinkString,
            onClick = {
                annotatedLinkString
                    .getStringAnnotations("URL", it, it)
                    .firstOrNull()?.let { stringAnnotation ->
                        uriHandler.openUri(stringAnnotation.item)
                    }
            }
        )
    }
}

//@Preview(showBackground = true, device = "id:pixel_5")
//@Composable
//fun AppConfigFormPreview() {
//    Homescreenvault2Theme {
//        AppConfigForm()
//    }
//}