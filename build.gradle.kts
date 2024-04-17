// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.devtools.ksp) apply false
    alias(libs.plugins.dagger.hilt.android) apply false
    alias(libs.plugins.roborazzi) apply false
//    id("com.android.library") version "8.3.0" apply false
//    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
//    id("com.google.dagger.hilt.android") version "2.51" apply false
//    id("com.google.devtools.ksp") version "1.9.23-1.0.19" apply false
}