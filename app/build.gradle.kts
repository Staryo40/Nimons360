import org.gradle.kotlin.dsl.resolver.SourceDistributionResolver.Companion.artifactType
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.ksp)
    id("com.google.gms.google-services")
    id("com.google.firebase.appdistribution")
}

// Properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

// Env
val env = Properties()
val envFile = rootProject.file(".env")

if (envFile.exists()) {
    envFile.inputStream().use { stream ->
        env.load(stream)
    }
}

val testersList = env.getProperty("FIREBASE_TESTERS") ?: ""
val firebaseAppId = env.getProperty("FIREBASE_APP_ID") ?: ""

android {
    namespace = "com.labpro.nimons360"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.labpro.nimons360"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "BASE_URL", "\"https://mad.labpro.hmif.dev\"")
        buildConfigField("String", "AGORA_APP_ID", "\"${localProperties.getProperty("AGORA_APP_ID") ?: ""}\"")
        buildConfigField("String", "FACEBOOK_APP_ID", "\"${localProperties.getProperty("FACEBOOK_APP_ID") ?: ""}\"")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    configure<com.google.firebase.appdistribution.gradle.AppDistributionExtension> {
        releaseNotes = "Initial testing build"
        testers = testersList
        artifactType = "APK"
        appId = firebaseAppId
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.coil.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.androidx.junit.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.google.material)
    implementation(libs.androidx.drawerlayout)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Room Database (Logic in data/local)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // EncryptedSharedPreferences (Logic in core/utils)
    implementation(libs.androidx.security.crypto)

    // Maps & Location
    implementation(libs.osmdroid)
    implementation(libs.play.services.location)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Media & WebRTC
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.google.webrtc)

    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    testImplementation(kotlin("test"))

    // livestreaming
    implementation("com.github.AgoraIO-Community:VideoUIKit-Android:4.0.1") {
        exclude(group = "com.github.AgoraIO-Community.VideoUIKit-Android", module = "final-debug")
    }

    // firebase
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-analytics")
}
