plugins {
    id("module")
}

val extraDepsToPackage by configurations.registering

dependencies {
    compileOnly(projects.detektApi)
    compileOnly(projects.detektPsiUtils)
    implementation(projects.detektFormatting.ktlintRepackage) {
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.SHADOWED))
        }
    }

    runtimeOnly(libs.slf4j.api)

    testImplementation(libs.kotlin.compiler)
    testImplementation(projects.detektTest)
    testImplementation(libs.assertj.core)
    testImplementation(libs.classgraph)

    testRuntimeOnly(libs.slf4j.nop)
    extraDepsToPackage(libs.slf4j.nop)
}

consumeGeneratedConfig(
    fromProject = projects.detektGenerator,
    fromConfiguration = "generatedFormattingConfig",
    forTask = tasks.sourcesJar
)
consumeGeneratedConfig(
    fromProject = projects.detektGenerator,
    fromConfiguration = "generatedFormattingConfig",
    forTask = tasks.processResources
)

val depsToPackage = setOf(
    "org.ec4j.core",
    "com.pinterest.ktlint",
    "io.github.oshai",
)

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE // allow duplicates
    dependsOn(configurations.runtimeClasspath, extraDepsToPackage)
    from(
        configurations.runtimeClasspath.get()
            .filter { dependency -> depsToPackage.any { it in dependency.toString() } }
            .map { if (it.isDirectory) it else zipTree(it) },
        extraDepsToPackage.get().map { zipTree(it) },
    )
}
