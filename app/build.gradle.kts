plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.baldbuffalo.behindthecreator"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.baldbuffalo.behindthecreator"
        minSdk = 24
        targetSdk = 35
        versionCode = 3
        versionName = "1.2"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    val youtubeApiKey = providers.gradleProperty("YOUTUBE_API_KEY").orNull ?: ""
    buildTypes {
        getByName("debug") {
            buildConfigField("String", "YOUTUBE_API_KEY", "\"$youtubeApiKey\"")
        }
        getByName("release") {
            buildConfigField("String", "YOUTUBE_API_KEY", "\"$youtubeApiKey\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(platform("androidx.compose:compose-bom:2025.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.7")
    implementation("io.coil-kt.coil3:coil-compose:3.1.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
