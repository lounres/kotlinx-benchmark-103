plugins {
    with(libs.plugins) {
        alias(kotlin.multiplatform)
        alias(allopen)
        alias(kotlinx.benchmark)
    }
}

allOpen.annotation("org.openjdk.jmh.annotations.State")

kotlin {
    explicitApi = org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode.Warning

    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
        testRuns.all {
            executionTask {
                useJUnitPlatform()
            }
        }
    }

    js(IR) {
        browser()
        nodejs()
    }

    linuxX64()
    mingwX64()
    macosX64()

    @Suppress("UNUSED_VARIABLE")
    sourceSets {
        all {
            languageSettings {
                progressiveMode = true
                optIn("kotlin.contracts.ExperimentalContracts")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }

    val commonBenchmarks by sourceSets.creating {
        dependencies {
            implementation(rootProject.libs.kotlinx.benchmark.runtime)
        }
    }
    targets.filter { it.platformType != org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.common }.forEach {
        it.compilations {
            val main by getting
            val benchmarks by creating {
                defaultSourceSet {
                    dependsOn(main.defaultSourceSet)
                    dependsOn(commonBenchmarks)
                }
            }

            // Only JVM targets are registered for now
            if (it.platformType == org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.jvm) {
                benchmark.targets.register(benchmarks.defaultSourceSetName)
            }
        }
    }
}

benchmark {
    configurations {
        val main by getting {
            warmups = 20
            iterations = 10
            iterationTime = 3
        }
        val smoke by creating {
            warmups = 5
            iterations = 3
            iterationTime = 500
            iterationTimeUnit = "ms"
        }
    }
}