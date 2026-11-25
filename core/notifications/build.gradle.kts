plugins {
    id("echolist.kmp.library")
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
}
kotlin.sourceSets.getByName("commonMain").dependencies {
    api(libs.koin.core)
    api(project(":core:deeplinks"))
    implementation(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.sqldelight.runtime)
    implementation(libs.sqldelight.async)
}

kotlin.sourceSets {
    getByName("androidMain").dependencies {
        implementation(libs.androidx.core)
        implementation(libs.androidx.work.runtime)
        implementation(libs.sqldelight.driver.android)
    }
    getByName("jvmMain").dependencies { implementation(libs.sqldelight.driver.jvm) }
    getByName("jvmTest").dependencies { implementation(libs.sqldelight.driver.jvm) }
    getByName("jsMain").dependencies { implementation(libs.kotlinx.browser) }
    getByName("wasmJsMain").dependencies { implementation(libs.kotlinx.browser) }
}

sqldelight {
    databases {
        create("NotificationDatabase") {
            packageName.set("net.onefivefour.echolist.core.notifications.database")
            generateAsync.set(true)
        }
    }
}