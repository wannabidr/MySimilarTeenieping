plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.sss.mysimilarteenieping"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sss.mysimilarteenieping"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.0.0"
        // 배포할 일이 있으면 proguard rule 세팅할 것

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
//            applicationIdSuffix '.prod'
//            resValue("string","BUILD_TYPE","release 타입 입니다.")
//            buildConfigField "boolean","IS_DEBUG","false"
            ndk {
                debugSymbolLevel = "Full"
            }
        }

        create("inhouse") {
            //내부 배포, 테스트
            initWith(getByName("release"))
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
        }

        getByName("debug") {
            //개발
            isMinifyEnabled = false
            isDebuggable = true
//            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'

//            applicationIdSuffix '.dev'
//            resValue("string","BUILD_TYPE","debug 타입 입니다.")
//            buildConfigField "boolean","IS_DEBUG","true"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    composeCompiler {
        enableStrongSkippingMode.set(true)
        includeSourceInformation.set(true)
        enableStrongSkippingMode.set(true)
        reportsDestination.set(layout.buildDirectory.dir("compose_compiler"))
        stabilityConfigurationFile.set(rootProject.layout.projectDirectory.file("stability_config.conf"))
    }
}

dependencies {

    compileOnly("org.jetbrains.kotlin:compose-compiler-gradle-plugin:${libs.versions.kotlin}")

    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.support)

    // for hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // for firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.ui.storage)
//    implementation("com.google.firebase:firebase-config")

    // for glide
    val glideVer = "com.github.bumptech.glide:glide:4.16.0"
    implementation(glideVer)
    ksp(glideVer)

    // for retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.scalars)
    implementation(libs.converter.gson)

    // for material icons
    implementation(libs.androidx.material.icons.extended)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}