import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
}

kotlin {
    //select iOS target platform depending on the Xcode environment variables
    val iOSTarget: (String, KotlinNativeTarget.() -> Unit) -> KotlinNativeTarget =
        if (System.getenv("SDK_NAME")?.startsWith("iphoneos") == true)
            ::iosArm64
        else
            ::iosX64

    iOSTarget("ios") {
        binaries {
            framework {
                baseName = "SharedCode"
            }
        }
    }

    jvm("android")

    sourceSets["iosMain"].dependencies {


        if(project.properties["updatedVersions"].toString().toBoolean()) {
            implementation("io.ktor:ktor-client-ios:1.4.0")
        } else {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:1.3.5-native-mt") {
                isForce = true
            }
            implementation("io.ktor:ktor-client-ios:1.3.2")
            implementation("io.ktor:ktor-client-logging-native:1.3.2")
        }
    }

    sourceSets["commonMain"].dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-common")

        if(project.properties["updatedVersions"].toString().toBoolean()) {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8-native-mt-1.4.0-rc")
            implementation("io.ktor:ktor-client-core:1.4.0")
            implementation("io.ktor:ktor-client-logging:1.4.0")
        } else {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.5-native-mt")
            implementation("io.ktor:ktor-client-core:1.3.2")
            implementation("io.ktor:ktor-client-logging:1.3.2")
        }
    }

    sourceSets["androidMain"].dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib")

        if(project.properties["updatedVersions"].toString().toBoolean()) {
            api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.8-native-mt-1.4.0-rc")
            implementation("io.ktor:ktor-client-android:1.4.0")
        } else {
            api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.5-native-mt")
            implementation("io.ktor:ktor-client-android:1.3.2")
            implementation("io.ktor:ktor-client-logging-jvm:1.3.2")
        }
    }
}


val packForXcode by tasks.creating(Sync::class) {
    group = "build"

    //selecting the right configuration for the iOS framework depending on the Xcode environment variables
    val mode = System.getenv("CONFIGURATION") ?: "DEBUG"
    val framework = kotlin.targets.getByName<KotlinNativeTarget>("ios").binaries.getFramework(mode)

    inputs.property("mode", mode)
    dependsOn(framework.linkTask)

    val targetDir = File(buildDir, "xcode-frameworks")
    from({ framework.outputDirectory })
    into(targetDir)

    doLast {
        val gradlew = File(targetDir, "gradlew")
        gradlew.writeText("#!/bin/bash\nexport 'JAVA_HOME=${System.getProperty("java.home")}'\ncd '${rootProject.rootDir}'\n./gradlew \$@\n")
        gradlew.setExecutable(true)
    }
}



tasks.getByName("build").dependsOn(packForXcode)
