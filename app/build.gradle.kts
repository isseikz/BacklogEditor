plugins {
    id("com.android.application")
    id("com.google.android.gms.oss-licenses-plugin")
    id("org.jetbrains.kotlin.android")
    id("com.apollographql.apollo3")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.isseikz.backlogeditor"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.isseikz.backlogeditor"
        minSdk = 31
        targetSdk = 34
        versionCode = 8
        versionName = "1.0.8"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("Boolean", "showDebugToast", "false")
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "debug"
            buildConfigField("Boolean", "showDebugToast", "true")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.compose.ui:ui-tooling-preview-android:1.5.4")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-common:1.1.0")
    implementation("androidx.hilt:hilt-work:1.1.0")
    implementation("androidx.datastore:datastore-core:1.0.0")
    implementation("androidx.datastore:datastore-preferences-core:1.0.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.security:security-crypto-ktx:1.1.0-alpha06")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.apollographql.apollo3:apollo-runtime:4.0.0-beta.4")
    implementation("com.jakewharton.timber:timber:5.0.1")

    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-android-compiler:2.50")
    kapt("androidx.hilt:hilt-compiler:1.1.0")

    implementation("androidx.datastore:datastore:1.0.0")
    implementation("com.google.android.gms:play-services-oss-licenses:17.0.1")

}

apollo {
    service("github") {
        sourceFolder.set("com/isseikz/backlogeditor")
        packageName.set("com.isseikz.backlogeditor")
    }
}

kapt {
    correctErrorTypes = true
}
