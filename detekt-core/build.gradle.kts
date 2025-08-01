plugins {
    id("module")
}

dependencies {
    api(projects.detektApi)
    api(projects.detektParser)
    api(projects.detektTooling)
    implementation(projects.detektKotlinAnalysisApiStandalone)
    implementation(libs.snakeyaml.engine)
    implementation(libs.kotlin.reflect)
    implementation(projects.detektMetrics)
    implementation(projects.detektPsiUtils)
    implementation(projects.detektUtils)

    testRuntimeOnly(projects.detektRules)
    testImplementation(projects.detektReportHtml)
    testImplementation(projects.detektReportMd)
    testImplementation(projects.detektReportXml)
    testImplementation(projects.detektTest)
    testImplementation(testFixtures(projects.detektApi))
    testImplementation(libs.classgraph)
    testImplementation(libs.assertj.core)
    testRuntimeOnly(libs.slf4j.simple)
}

consumeGeneratedConfig(
    fromProject = projects.detektGenerator,
    fromConfiguration = "generatedCoreConfig",
    forTask = tasks.sourcesJar
)
