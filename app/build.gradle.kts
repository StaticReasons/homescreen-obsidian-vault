plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    kotlin("plugin.serialization") version "1.9.23"
    kotlin("kapt")
//    id("com.android.application")
    id("com.google.dagger.hilt.android")
    id("com.google.protobuf") version "0.9.4"
}

android {
    namespace = "com.hyperrecursion.home_screen_vault2"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.hyperrecursion.home_screen_vault2"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    buildFeatures {
        compose = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildFeatures {
        viewBinding = true
    }

//    sourceSets {
//        main {
//            proto {
//                ...
//            }
//            java {
//                ...
//            }
//        }
//    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.work.runtime.ktx)
    implementation("androidx.work:work-runtime:2.9.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // --------------------------------------------------------------------------
    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // --------------------------------------------------------------------------
    // DataStore
    implementation(libs.androidx.datastore.preferences)
    // optional - RxJava2 support
    implementation(libs.androidx.datastore.preferences.rxjava2)
    // optional - RxJava3 support
    implementation(libs.androidx.datastore.preferences.rxjava3)

    implementation(libs.androidx.datastore)
    // optional - RxJava2 support
    implementation(libs.androidx.datastore.rxjava2)
    // optional - RxJava3 support
    implementation(libs.androidx.datastore.rxjava3)

    implementation(libs.protobuf.javalite)

    // --------------------------------------------------------------------------
    // coroutines
    implementation(libs.kotlinx.coroutines.android)

    // --------------------------------------------------------------------------
    // Hell Hilt***!
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    // With WorkManager
    implementation(libs.androidx.hilt.work)
    kapt(libs.androidx.hilt.compiler.v100)

    // --------------------------------------------------------------------------
    // KTXs
    implementation(libs.androidx.fragment.ktx)


    // --------------------------------------------------------------------------
    // Jetpack Compose

    val composeBom = platform("androidx.compose:compose-bom:2024.03.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Choose one of the following:
    // Material Design 3
    implementation("androidx.compose.material3:material3")
    // or Material Design 2
//    implementation("androidx.compose.material:material")
    // or skip Material Design and build directly on top of foundational components
//    implementation("androidx.compose.foundation:foundation")
    // or only import the main APIs for the underlying toolkit systems,
    // such as input and measurement/layout
//    implementation("androidx.compose.ui:ui")

    // Android Studio Preview support
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // UI Tests
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Optional - Included automatically by material, only add when you need
    // the icons but not the material library (e.g. when using Material3 or a
    // custom design system based on Foundation)
//    implementation("androidx.compose.material:material-icons-core")
//    // Optional - Add full set of material icons
//    implementation("androidx.compose.material:material-icons-extended")
//    // Optional - Add window size utils
//    implementation("androidx.compose.material3:material3-window-size-class")

    // Optional - Integration with activities
    implementation(libs.androidx.activity.compose)
    // Optional - Integration with ViewModels
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // Optional - Integration with LiveData
    implementation("androidx.compose.runtime:runtime-livedata")
    // Optional - Integration with RxJava
    implementation("androidx.compose.runtime:runtime-rxjava2")


    // --------------------------------------------------------------------------
    // Jetpack Glance
    // For AppWidgets support
    implementation(libs.androidx.glance.appwidget)

//    // For interop APIs with Material 2
    implementation(libs.androidx.glance.material)

    // For interop APIs with Material 3
    implementation(libs.androidx.glance.material3)

    // Preview support
    implementation(libs.androidx.glance.preview)

}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.14.0"
    }

    // Generates the java Protobuf-lite code for the Protobufs in this project. See
    // https://github.com/google/protobuf-gradle-plugin#customizing-protobuf-compilation
    // for more information.
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite");
                }
            }
        }
    }
}