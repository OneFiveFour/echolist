plugins { id("echolist.compose.library") }
kotlin.sourceSets.getByName("commonMain").dependencies {
    api(libs.koin.core)
    implementation(project(":core:designsystem"))
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)
    api(libs.kotlinx.coroutines.core)
    api(project(":core:tasks"))
    implementation(project(":core:notifications"))
    implementation(project(":core:files"))
    implementation(libs.kotlinx.datetime)
    implementation(libs.compose.materialIconsExtended)
}

compose.resources { packageOfResClass = "net.onefivefour.echolist.feature.tasklist.resources" }