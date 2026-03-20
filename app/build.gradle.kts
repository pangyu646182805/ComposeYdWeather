plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.yd.weather"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.yd.weather"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // ABI 分包配置 - 一次性打包多个架构版本
    splits {
        abi {
            // 启用 ABI 分包
            isEnable = true
            // 重置默认列表
            reset()
            // 包含的架构：32位和64位 ARM
            include("armeabi-v7a", "arm64-v8a")
            // 是否生成通用 APK（包含所有架构）
            // 设置为 true 会额外生成一个包含所有架构的 APK
            isUniversalApk = false
        }
    }

    signingConfigs {
        create("keyStoreDebug") {
            keyAlias = "CashBee"
            keyPassword = "cashbee2025"
            storeFile = file("CashBee.jks")
            storePassword = "cashbee2025"

            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
        create("keyStoreRelease") {
            keyAlias = "CashBee"
            keyPassword = "cashbee2025"
            storeFile = file("CashBee.jks")
            storePassword = "cashbee2025"

            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    buildTypes {
        debug {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("keyStoreDebug")
            buildConfigField("Boolean", "DEBUG", "true")
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("keyStoreRelease")
            buildConfigField("Boolean", "DEBUG", "false")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.mmkv)
    implementation(libs.kotlinx.serialization.json)
    compileOnly(libs.ksp.gradlePlugin)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)
    implementation(libs.navigation.compose)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.okhttp3)
    implementation(libs.retrofit)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit2.kotlinx.serialization.converter)
    implementation(libs.toaster)
    implementation(libs.logcat)
    implementation(libs.xxpermissions)
    implementation(libs.reorderable)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.backdrop)
}