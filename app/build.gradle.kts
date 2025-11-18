plugins {
    id("com.android.application")
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.aulago"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.aulago"
        minSdk = 24
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

    //bia
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.volley)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:34.2.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth:22.3.0")
    implementation("com.google.android.material:material:1.13.0")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.0")
    implementation("com.github.ismaeldivita:chip-navigation-bar:1.4.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.security:security-crypto:1.0.0")
}