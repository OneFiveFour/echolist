plugins {
    id("echolist.compose.library")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":features:browser"))
            implementation(project(":features:tasknotifications"))
            implementation(project(":features:login"))
            implementation(project(":features:note"))
            implementation(project(":features:tasklist"))
            implementation(project(":features:tasksettings"))
            implementation(project(":core:database"))
            implementation(project(":core:designsystem"))
            implementation(project(":core:files"))
            implementation(project(":core:notifications"))
            implementation(project(":core:session"))
            implementation(project(":core:tasks"))
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodelNavigation3)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies { implementation(project(":core:networking")) }
        jvmTest.dependencies {
            implementation(libs.sqldelight.driver.jvm)
            implementation(libs.koin.test)
        }
    }
}

compose.resources { packageOfResClass = "net.onefivefour.echolist.app.resources" }