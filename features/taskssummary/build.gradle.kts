plugins { id("echolist.compose.library") }
kotlin.sourceSets.getByName("commonMain").dependencies {
    implementation(libs.compose.materialIconsExtended)
    api(libs.koin.core)
    implementation(project(":core:designsystem"))
    implementation(project(":core:tasks"))
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
}

compose.resources { packageOfResClass = "net.onefivefour.echolist.feature.taskssummary.resources" }