plugins { id("echolist.compose.library") }
kotlin.sourceSets.getByName("commonMain").dependencies {
    implementation(libs.compose.material3.navigationSuite)
    api(libs.koin.core)
    implementation(project(":core:designsystem"))
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.navigationevent.compose)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation(project(":core:files"))
    implementation(project(":core:networking"))
    implementation(project(":core:protocol"))
    implementation(project(":core:session"))
    implementation(libs.compose.materialIconsExtended)
}

compose.resources { packageOfResClass = "net.onefivefour.echolist.feature.browser.resources" }