plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
dependencies{
    implementation(libs.javax)
    implementation(libs.kotlinx.coroutine)
    implementation(libs.kotlinx.serialization.json)
    // BOM needed here since :domain is a JVM module without the compose convention plugin
    compileOnly(platform(libs.compose.bom))
    compileOnly(libs.compose.runtime)
}