import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidxRoom)
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
        iosTarget.binaries.framework {
            export(libs.kmpnotifier)
            baseName = "ComposeApp"
            linkerOpts.add("-lsqlite3")
            isStatic = true
        }
    }
    
    /*jvm()*/

    sourceSets.commonMain {
        kotlin.srcDir("build/generated/ksp/metadata")
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)

            implementation(libs.ktor.client.okhttp)

            implementation(libs.androidx.room.runtime)

            implementation(libs.androidx.location.service)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
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

            api(libs.kmpnotifier)

        }

        nativeMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        /*jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.ktor.client.okhttp)
        }*/
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
}

dependencies {
    debugImplementation(compose.uiTooling)
}

/*compose.desktop {
    application {
        mainClass = "com.kronos.multiplatform.weatherapp.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.kronos.multiplatform.weatherapp"
            packageVersion = "1.0.0"
        }
    }
}*/

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
