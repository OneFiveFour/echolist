plugins { id("echolist.compose.library") }
kotlin.sourceSets.getByName("commonMain").dependencies {
    api(libs.koin.core)
    implementation(project(":core:designsystem"))
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.kotlinx.coroutines.core)
    implementation(project(":core:session"))
}
kotlin.sourceSets.getByName("commonTest").dependencies { implementation(project(":core:networking")) }

compose.resources { packageOfResClass = "net.onefivefour.echolist.feature.login.resources" }