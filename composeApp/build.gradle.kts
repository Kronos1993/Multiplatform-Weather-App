import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URI

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidxRoom)
    alias(libs.plugins.swiftPackageManager)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosX64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.compilations {
            val main by getting {
                cinterops.create("maplibre")
            }
        }

        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            linkerOpts.add("-lsqlite3")
            isStatic = true
        }
    }

    sourceSets.commonMain {
        kotlin.srcDir("build/generated/ksp/metadata")
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)

            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)

            implementation(libs.ktor.client.okhttp)

            implementation(libs.androidx.room.runtime)

            implementation(libs.androidx.location.service)

            // MapLibre para Android
            //implementation(libs.maplibre.android)

            //Glace Widget
            implementation(libs.androidx.glance.appwidget)
            implementation(libs.androidx.glance.material3)

            //worker
            implementation(libs.androidx.work.runtime.ktx)
        }
        commonMain.dependencies {
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material)
            implementation(libs.material3)
            implementation(libs.ui)
            implementation(libs.components.resources)
            implementation(libs.ui.tooling.preview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.material3.adaptive)

            implementation(libs.navigation.compose)
            api(libs.datastore.preferences)
            api(libs.datastore)

            implementation(libs.coil.compose.core)
            implementation(libs.coil.compose)
            implementation(libs.coil.mp)
            implementation(libs.coil.network.ktor)

            api(libs.moko.permissions)
            api(libs.moko.permissions.compose)
            api(libs.moko.permissions.notifications)
            api(libs.moko.permissions.storage)
            api(libs.moko.permissions.location)

            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.navigation)
            implementation(libs.koin.annotations)

            implementation(libs.extended.icons)

            implementation(libs.bundles.ktor)

            implementation(libs.kotlinx.datetime)

            //logger
            implementation(libs.kermit)

            //room
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqliteBundled)

            //file kit
            /*implementation(libs.file.kit.core)
            implementation(libs.file.kit.compose)*/

            api(libs.kmpnotifier)

            implementation(libs.maplibre.compose)
            implementation(libs.maplibre.composeMaterial3)

            //charts
            implementation (libs.compose.charts)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }

    swiftPackageConfig {
        create("maplibre") {
            dependency {
                // Usar el repositorio principal que tiene mejor estructura
                remotePackageVersion(
                    url = URI("https://github.com/maplibre/maplibre-gl-native-distribution.git"),
                    products = { add("MapLibre") },
                    version = "6.17.1"
                )
            }
        }
    }
}

android {
    namespace = "com.kronos.multiplatform.weatherapp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.kronos.multiplatform.weatherapp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    debugImplementation(compose.uiTooling)
    ksp(libs.androidx.room.compiler)
    add("kspCommonMainMetadata", libs.androidx.room.compiler)
    add("kspAndroid",libs.androidx.room.compiler)
    add("kspIosSimulatorArm64",libs.androidx.room.compiler)
    add("kspIosX64",libs.androidx.room.compiler)
    add("kspIosArm64",libs.androidx.room.compiler)
}