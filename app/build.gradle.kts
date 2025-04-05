plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "app.compile"
    compileSdk = 35
    buildToolsVersion = "35.0.0"
    
    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    
    defaultConfig {
        applicationId = "adb.shell.shizuku"
        minSdk = 24
        targetSdk = 35
        versionCode = 8
        versionName = "v0.0.8"
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        vectorDrawables { 
            useSupportLibrary = true
        }
    }
    
    signingConfigs {
        create("url") {
        // keystore file，.bks & .jks
            storeFile = file("keystore/xiao.keystore")
            storePassword = findProperty("KEYSTORE_PASSWORD") as String
            keyAlias = findProperty("KEY_ALIAS") as String
            keyPassword = findProperty("KEY_PASSWORD") as String
            
            enableV1Signing = false
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("url")
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            signingConfig = signingConfigs.getByName("url")
        }
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        aidl = true
        buildConfig = true
    }
    
    kotlinOptions {
        jvmTarget = "21"
    }
    
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
    
    lint {
        htmlReport = true
        // disable("ProtectedPermissions")
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    
}

tasks
    .withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>()
    .configureEach {
        compilerOptions
            .jvmTarget
            .set(
                org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
            )
    }

dependencies {
    val lifecycle_version = "2.8.7"
    val shizuku_version = "13.1.5"
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-ktx:1.10.1")
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm")
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.lsposed.hiddenapibypass:hiddenapibypass:4.3")
    implementation("dev.rikka.shizuku:api:$shizuku_version")
    implementation("dev.rikka.shizuku:provider:$shizuku_version")
    implementation(fileTree("configs") { include("*.jar") })
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation("androidx.lifecycle:lifecycle-process:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version")
}