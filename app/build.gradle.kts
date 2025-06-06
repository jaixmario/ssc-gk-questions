plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.mario.ssc"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mario.ssc"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.squareup.okio:okio:3.8.0")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("com.google.android.material:material:1.14.0-alpha01")
}