plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
    alias(libs.plugins.ktfmt)
}

ktfmt {
    kotlinLangStyle()
}

val ktfmtCheck = tasks.named("ktfmtCheck")

tasks.register("prePush") {    dependsOn(ktfmtCheck)
    group = "validation"
}
