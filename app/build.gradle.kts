plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.roborazzi)
//    id ("org.jetbrains.kotlin.android")
//    id ("com.google.devtools.ksp")
    id ("kotlin-parcelize")
    id ("dagger.hilt.android.plugin")
}

android {
    namespace = "com.zzy.champions"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.zzy.champions"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

//    signingConfigs {
//        create("release") {
//            storeFile = file("release-key.jks")
//            storePassword = project.property("KEYSTORE_PASSWORD").toString()
//            keyAlias = project.property("SIGNING_KEY_ALIAS").toString()
//            keyPassword = project.property("SIGNING_KEY_PASSWORD").toString()
//        }
//    }

    buildTypes {
        debug {
            isDebuggable = true
        }

        release {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
//    packagingOptions {
//        resources {
//            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
//        }
//    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            merges += "META-INF/LICENSE.md"
            merges += "META-INF/LICENSE-notice.md"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

//kapt {
//    correctErrorTypes true
//}
//
//tasks.withType(KotlinCompile).configureEach {
//    kotlinOptions {
//        // Treat all Kotlin warnings as errors
////        allWarningsAsErrors = true
//
//        freeCompilerArgs += '-opt-in=kotlin.RequiresOptIn'
//
//        // Set JVM target to 1.8
//        jvmTarget = "1.8"
//    }
//}

dependencies {
    val composeBom = platform(libs.android.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.android.compose.runtime)
    implementation(libs.android.compose.foundation)
    implementation(libs.android.compose.ui)
    implementation(libs.android.compose.ui.tooling)
    implementation(libs.android.compose.ui.test.manifest)
    androidTestImplementation(libs.android.compose.ui.test)
    androidTestImplementation(libs.android.compose.ui.test.junit4)
    debugImplementation(libs.android.compose.ui.test.manifest)

    implementation(libs.android.appcompat)
    implementation(libs.android.ktx)
    implementation(libs.android.activity.compose)
    implementation(libs.android.material3)
    implementation(libs.android.datastore.preferences)

    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.viewmodel.compose)

    implementation(libs.navigation.compose)
    androidTestImplementation(libs.navigation.testing)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)

    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.dagger.compiler)
    ksp(libs.dagger.hilt.compiler)

    implementation(libs.android.hilt.navigation.compose)

    implementation(libs.coil)
    implementation(libs.coil.compose)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    testImplementation(libs.room.testing)


    testImplementation(libs.junit)
    testImplementation(libs.coroutine.test)
    testImplementation(libs.mockk.android)
    testImplementation(libs.mockk.agent)

//    testImplementation(libs.espresso.core)
//    testImplementation(libs.ui.test.junit4)
    testImplementation(libs.android.compose.ui.test.junit4)

    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.rule)
    testImplementation(libs.accompanist.testharness)

    androidTestImplementation(libs.android.test.junit)
    androidTestImplementation(libs.android.test.espresso)
    androidTestImplementation(libs.android.test.runner)
    androidTestImplementation(libs.android.test.rules)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.mockk.agent)

//    implementation("androidx.appcompat:appcompat:1.6.1")
//    implementation("androidx.core:core-ktx:1.12.0")
//    implementation("androidx.activity:activity-compose:1.7.2")
//    implementation("androidx.compose.material3:material3:1.1.2")
//    implementation("androidx.datastore:datastore-preferences:1.0.0")
//    val lifecycle_version = "2.6.1"
//    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version")
//    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycle_version")
//    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
//    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version")
//    val nav_version = "2.7.4"
//    implementation("androidx.navigation:navigation-compose:$nav_version")
//    androidTestImplementation("androidx.navigation:navigation-testing:$nav_version")
//    val retrofit_version = "2.9.0"
//    implementation("com.squareup.retrofit2:retrofit:$retrofit_version")
//    implementation("com.squareup.retrofit2:converter-moshi:$retrofit_version")
//    val moshi_version = "1.15.0"
//    implementation("com.squareup.moshi:moshi-kotlin:$moshi_version")
//    ksp("com.squareup.moshi:moshi-kotlin-codegen:$moshi_version")
//    val okhttp_version = "4.10.0"
//    implementation("com.squareup.okhttp3:okhttp:$okhttp_version")
//    implementation("com.squareup.okhttp3:logging-interceptor:$okhttp_version")
//    val hilt_version = "2.51"
//    implementation("com.google.dagger:hilt-android:$hilt_version")
//    ksp("com.google.dagger:dagger-compiler:$hilt_version") // Dagger compiler
//    ksp("com.google.dagger:hilt-compiler:$hilt_version")
//    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
//    val coil_version = "2.4.0"
//    implementation("io.coil-kt:coil:$coil_version")
//    implementation("io.coil-kt:coil-compose:$coil_version")
//    val room_version = "2.6.1"
//    implementation("androidx.room:room-runtime:$room_version")
//    implementation("androidx.room:room-ktx:$room_version")
//    ksp("androidx.room:room-compiler:$room_version")
//    testImplementation("androidx.room:room-testing:$room_version")
//    testImplementation("junit:junit:4.13.2")
//    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
//    val mockk_version = "1.13.4"
//    testImplementation("io.mockk:mockk-android:$mockk_version")
//    testImplementation("io.mockk:mockk-agent:$mockk_version")
//    androidTestImplementation("androidx.test.ext:junit:1.1.5")
//    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
//    androidTestImplementation("androidx.test:runner:1.4.0")
//    androidTestImplementation("androidx.test:rules:1.4.0")
}