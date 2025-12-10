import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.tekbuk"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.tekbuk"
        minSdk = 26
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
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.gridlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // --- Firebase & Google Authentication (Corrected Block) ---

    // 1. Import the Firebase Bill of Materials (BoM)
    // This manages versions for all Firebase libraries to ensure compatibility.
    implementation(platform("com.google.firebase:firebase-bom:33.1.0")) // Updated to a recent stable version

    // 2. Add the specific Firebase products you need (without versions).
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-firestore")

    // 3. Add the Google Sign-In library (part of Play Services, not Firebase BoM)
    implementation("com.google.android.gms:play-services-auth:21.2.0") // Updated to a recent stable version


    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // Add the dependencies for Firebase products you need
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

}