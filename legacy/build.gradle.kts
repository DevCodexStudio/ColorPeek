import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.4.10"
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "online.devcodex.colorpeek"
version = "1.0.0-legacy.232"

repositories {
    mavenCentral()
    intellijPlatform { defaultRepositories() }
}

dependencies {
    intellijPlatform {
        intellijIdea("2023.2.8")
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.kotlin")
        testFramework(TestFrameworkType.Platform)
    }
    testImplementation(kotlin("test-junit"))
    testImplementation("junit:junit:4.13.2")
}

sourceSets {
    main {
        kotlin.srcDir(rootProject.file("src/main/kotlin"))
    }
    test {
        kotlin.srcDir(rootProject.file("src/test/kotlin"))
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

intellijPlatform {
    projectName = "ColorPeek"
    // IntelliJ Platform Gradle Plugin 2.x no longer launches pre-233 IDEs to
    // generate this optional index. The configurable remains discoverable by
    // its display name and parent in plugin.xml.
    buildSearchableOptions = false
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "232"
            untilBuild = "241.*"
        }
    }
}
