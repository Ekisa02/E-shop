plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.joseph.e_electronicshop"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.joseph.e_electronicshop"
        minSdk = 21
        targetSdk = 36
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

        // Add these if not already present
        implementation ("androidx.recyclerview:recyclerview:1.3.2")
        implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.recyclerview)
    implementation(libs.play.services.analytics.impl)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
        implementation ("com.google.android.material:material:1.11.0")

    implementation(libs.appcompat)
    implementation(libs.material.v160)
    // Image picker and cropping
    implementation(libs.activity.ktx)
    implementation(libs.ucrop.v226native)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.activity)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}