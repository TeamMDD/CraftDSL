plugins {
    common

    id("com.gradleup.shadow") version "9.0.0-beta8"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.14"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

repositories {
    mavenLocal()
}

dependencies {
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
    api(project(":script"))

    implementation("kr.alpha93:dokdo:1.0.0-SNAPSHOT")
    implementation("kr.alpha93.ph:paper:1.0.0-SNAPSHOT:dev-all")

    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(kotlin("scripting-jvm"))
    implementation(kotlin("scripting-jvm-host"))
    implementation(kotlin("scripting-dependencies"))
    implementation(kotlin("scripting-dependencies-maven"))

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
    // coroutines dependency is required for this particular definition
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
}

tasks.shadowJar {
    dependsOn(project(":script").tasks.build)
    archiveClassifier = ""

    from(project(":script").tasks.jar.get().archiveFile.get().asFile) { include("*.jar") }
    dependencies { exclude { !it.moduleGroup.startsWith("kr.alpha93") } }
}

tasks.build { dependsOn(tasks.shadowJar) }

tasks.withType<xyz.jpenilla.runtask.task.AbstractRun> {
    javaLauncher = javaToolchains.launcherFor {
        vendor = @Suppress("UnstableApiUsage") JvmVendorSpec.JETBRAINS
        languageVersion = JavaLanguageVersion.of(21)
    }
    jvmArgs("-XX:+AllowEnhancedClassRedefinition")
}
