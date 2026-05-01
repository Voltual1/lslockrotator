import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
}

// 读取本地的 keystore.properties（如果存在）
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

android {
    namespace = "me.voltual.lslockrotator"
    compileSdk = gropify.project.android.compileSdk

    defaultConfig {
        applicationId = "me.voltual.lslockrotator"
        minSdk = gropify.project.android.minSdk
        targetSdk = gropify.project.android.targetSdk
        versionName = gropify.project.samples.demo.module.versionName
        versionCode = gropify.project.samples.demo.module.versionCode
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            // 优先使用环境变量，其次使用本地 properties，最后回退到 debug.keystore
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: keystoreProperties.getProperty("storeFile") ?: "debug.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: keystoreProperties.getProperty("storePassword")
            keyAlias = System.getenv("KEY_ALIAS") ?: keystoreProperties.getProperty("keyAlias")
            keyPassword = System.getenv("KEY_PASSWORD") ?: keystoreProperties.getProperty("keyPassword")
        }
    }

    // 自定义输出的 APK 文件名
    applicationVariants.all {
        val variant = this
        outputs.all {
            val output = this as com.android.build.gradle.internal.api.ApkVariantOutputImpl
            output.outputFileName = "LSLockRotator-${variant.versionName}-${variant.buildType.name}.apk"
        }
    }

    buildTypes {
        release {
            // 开启混淆和资源压缩，让无 UI 模块体积极致小
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
        // 移除了 viewBinding，因为我们不需要 UI 了
    }

    lint { checkReleaseBuilds = false }
    androidResources.additionalParameters += listOf("--allow-reserved-package-id", "--package-id", "0x64")
}

dependencies {
    // Xposed 核心 API
    compileOnly(libs.rovo89.xposed.api)
    
    // YukiHookAPI 核心与 KSP 处理程序
    implementation(projects.yukihookapiCore)
    ksp(projects.yukihookapiKspXposed)
    
    // KavaRef 反射库 (YukiHookAPI 1.3+ 推荐)
    implementation(libs.kavaref.core)
    implementation(libs.kavaref.extension)
    
    // 移除了所有不必要的 UI、ViewModel、Material 等依赖，保持模块纯净
}