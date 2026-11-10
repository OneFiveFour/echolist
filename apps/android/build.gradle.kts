import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
}
kotlin { compilerOptions { jvmTarget.set(JvmTarget.JVM_11) } }
dependencies {
    implementation(project(":app"))
    implementation(project(":core:notifications"))
    debugImplementation(project(":core:tasks"))
    implementation(project(":core:designsystem"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.uiToolingPreview)
    implementation(libs.koin.android)
}
// Load signing properties from local file (not in VCS)
val signingPropsFile = rootProject.file("signing.properties")
val signingProps = Properties().apply {
    if (signingPropsFile.exists()) {
        signingPropsFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "net.onefivefour.echolist"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    val releaseKeystorePropertiesFile = rootProject.file("keystore.properties")
    val releaseKeystoreProperties = Properties().apply {
        if (releaseKeystorePropertiesFile.exists()) {
            releaseKeystorePropertiesFile.inputStream().use(::load)
        }
    }
    fun releaseSigningProperty(name: String): String? =
        releaseKeystoreProperties.getProperty(name)?.takeIf { it.isNotBlank() }

    val hasReleaseSigningConfig = listOf(
        "storeFile",
        "storePassword",
        "keyAlias",
        "keyPassword"
    ).all { releaseSigningProperty(it) != null }

    defaultConfig {
        applicationId = "net.onefivefour.echolist"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    signingConfigs {
        create("release") {
            val storeFilePath = signingProps.getProperty("RELEASE_STORE_FILE")
            if (storeFilePath != null) {
                storeFile = rootProject.file(storeFilePath)
                storePassword = signingProps.getProperty("RELEASE_STORE_PASSWORD")
                keyAlias = signingProps.getProperty("RELEASE_KEY_ALIAS")
                keyPassword = signingProps.getProperty("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (signingPropsFile.exists()) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}