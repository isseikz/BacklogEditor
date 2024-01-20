// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.apollographql.apollo3") version "4.0.0-beta.4"
    id("com.google.dagger.hilt.android") version "2.50" apply false
}
buildscript {
    repositories {
        // Other repositories
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.1")
        classpath("com.apollographql.apollo3:apollo-gradle-plugin:4.0.0-beta.4")
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.6")
    }
}
