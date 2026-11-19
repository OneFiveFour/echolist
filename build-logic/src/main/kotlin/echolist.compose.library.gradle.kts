plugins {
    id("echolist.kmp.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
dependencies {
    // Common-code Android previews need the renderer in each previewed module.
    add("androidRuntimeClasspath", catalog.findLibrary("compose-uiTooling").get())
}

kotlin.sourceSets.getByName("commonMain").dependencies {
    implementation(catalog.findLibrary("compose-runtime").get())
    implementation(catalog.findLibrary("compose-foundation").get())
    implementation(catalog.findLibrary("compose-material3").get())
    implementation(catalog.findLibrary("compose-ui").get())
    implementation(catalog.findLibrary("compose-components-resources").get())
    implementation(catalog.findLibrary("compose-uiToolingPreview").get())
}

kotlin {
    sourceSets {
        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}
