plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.roborazzi)
    id ("kotlin-parcelize")
    id ("dagger.hilt.android.plugin")
}

android {
    namespace = "com.zzy.champions"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.zzy.champions"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
            // TODO: replace with a real release keystore (see commented signingConfigs block above);
            // debug signing is a placeholder so release builds remain installable in the meantime.
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
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

    implementation(libs.android.ktx)
    implementation(libs.android.appcompat)
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

    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

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
}