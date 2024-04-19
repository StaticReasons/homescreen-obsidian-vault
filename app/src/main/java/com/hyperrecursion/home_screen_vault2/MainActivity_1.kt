//package com.hyperrecursion.home_screen_vault2
//
//import android.net.Uri
//import android.os.Bundle
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material3.Button
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.input.ImeAction
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.datastore.core.DataStore
//import androidx.datastore.core.DataStoreFactory
//import com.hyperrecursion.home_screen_vault2.ui.theme.Homescreenvault2Theme
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.flow.flowOf
//import kotlinx.coroutines.launch
//
//class MainActivity_1 : ComponentActivity() {
//
//    private fun startFolderPicker(callback: (Uri?) -> Unit){
//        val folderPickerLauncher = this.registerForActivityResult(
//            ActivityResultContracts.OpenDocumentTree()
//        ) { uri -> callback(uri) }
//        folderPickerLauncher.launch(null)
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        val dataStore =
//            DataStoreFactory.create(serializer = AppConfigDataStoreSerializer,
//                produceFile = { filesDir.resolve("app_config.json") })
//
//        setContent {
//            Homescreenvault2Theme {
//                // A surface container using the 'background' color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
//                ) {
//                    AppConfigForm(dataStore, Modifier, onSave = {
//                        val toastText = "App configuration saved successfully"
//                        val toast = Toast.makeText(this, toastText, Toast.LENGTH_SHORT)
//                        toast.show()
//                    },
//                        pickFolderAction = this.startFolderPicker)
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun AppConfigForm(
//    dataStore: DataStore<AppConfig>,
//    modifier: Modifier = Modifier,
//    onSave: () -> Unit = {},
//    pickFolderAction: (callback: (Uri?) -> Unit) -> Unit = {}
//) {
//    val coroutineScope = rememberCoroutineScope()
//    var appConfig by remember { mutableStateOf(AppConfig()) }
//    dataStore.data.collectAsState(initial = AppConfig()).value.let { appConfig = it }
//
//    Column(modifier = modifier.padding(16.dp)) {
//        Text(
//            text = "High Frequency Scan Interval (seconds)",
//            modifier = Modifier.padding(bottom = 4.dp)
//        )
//        OutlinedTextField(
//            value = appConfig.highFreqScanIntervalSecs.toString(),
//            onValueChange = { appConfig = appConfig.copy(highFreqScanIntervalSecs = it.toInt()) },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 16.dp)
//        )
//
//        Text(
//            text = "Low Frequency Scan Interval (seconds)",
//            modifier = Modifier.padding(bottom = 4.dp)
//        )
//        OutlinedTextField(
//            value = appConfig.lowFreqScanIntervalSecs.toString(),
//            onValueChange = { appConfig = appConfig.copy(lowFreqScanIntervalSecs = it.toInt()) },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 16.dp)
//        )
//
//        Text(text = "Vault Path", modifier = Modifier.padding(bottom = 4.dp))
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 16.dp)
//        ) {
//            OutlinedTextField(
//                value = appConfig.vaultPath,
//                onValueChange = { appConfig = appConfig.copy(vaultPath = it) },
////                label = { Text(text = "Vault Path") },
//                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
////                keyboardActions = KeyboardActions(onDone = { vaultPath.value = tempVaultPath }),
//                singleLine = true,
//                modifier = Modifier
//                    .weight(1f)
//                    .padding(end = 8.dp)
//            )
//            IconButton(
//                onClick = {
//                    pickFolderAction {
//                        it?.path?.let { it1 -> appConfig = appConfig.copy(vaultPath = it1) }
//                    }
//                },
//                modifier = Modifier.size(32.dp)
//            ) {
//                Icon(
//                    painterResource(id = R.drawable.outline_folder_24),
//                    contentDescription = "Choose folder"
//                )
//            }
//        }
//
//        Button(
//            onClick = {
//                coroutineScope.launch {
//                    dataStore.updateData { appConfig }
//                    onSave()
//                }
//            }, modifier = Modifier.align(Alignment.End)
//        ) {
//            Text(text = "Save")
//        }
//    }
//}
//
//@Preview(showBackground = true, device = "id:pixel_5")
//@Composable
//fun AppConfigFormPreview() {
//    val mockDataStore = object : DataStore<AppConfig> {
//        override val data: Flow<AppConfig> = flowOf(AppConfig())
//        override suspend fun updateData(transform: suspend (t: AppConfig) -> AppConfig): AppConfig {
//            return transform(data.first())
//        }
//    }
//    Homescreenvault2Theme {
//        AppConfigForm(mockDataStore)
//    }
//}