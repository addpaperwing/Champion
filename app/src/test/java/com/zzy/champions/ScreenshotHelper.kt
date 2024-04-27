/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zzy.champions

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziOptions.CompareOptions
import com.github.takahirom.roborazzi.RoborazziOptions.RecordOptions
import com.github.takahirom.roborazzi.captureRoboImage
import com.google.accompanist.testharness.TestHarness
import com.zzy.champions.ui.theme.MyApplicationTheme
import org.robolectric.RuntimeEnvironment

private const val PATH = "src/test/screenshots/"

@OptIn(ExperimentalRoborazziApi::class)
val DefaultRoborazziOptions =
    RoborazziOptions(
        // Pixel-perfect matching
        compareOptions = CompareOptions(changeThreshold = 0f),
        // Reduce the size of the PNGs
        recordOptions = RecordOptions(resizeScale = 0.5),
    )

enum class DefaultTestDevices(val description: String, val spec: String) {
    PHONE("phone", "spec:shape=Normal,width=360,height=720,unit=dp,dpi=480"),
//    FOLDABLE("foldable", "spec:shape=Normal,width=673,height=841,unit=dp,dpi=480"),
//    TABLET("tablet", "spec:shape=Normal,width=1280,height=800,unit=dp,dpi=480"),
}
fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.captureMultiDevice(
    screenshotName: String,
    body: @Composable () -> Unit,
) {
    DefaultTestDevices.entries.forEach {
        this.captureForDevice(it.description, it.spec, screenshotName, body = body)
    }
}

fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.captureForPhone(
    screenshotName: String,
    body: @Composable () -> Unit,
) {
    this.captureForDevice(DefaultTestDevices.PHONE.description, DefaultTestDevices.PHONE.spec, screenshotName, body = body)
}

fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.setContentForDevice(
    device: DefaultTestDevices,
    darkMode: Boolean = false,
    body: @Composable () -> Unit,
) {
    val (width, height, dpi) = extractSpecs(device.spec)

    // Set qualifiers from specs
    RuntimeEnvironment.setQualifiers("w${width}dp-h${height}dp-${dpi}dpi")

    this.activity.setContent {
        CompositionLocalProvider(
            LocalInspectionMode provides true,
        ) {

            TestHarness(darkMode = darkMode) {
                body()
            }
        }
    }
}

fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.capture(
    deviceName: String,
    screenshotName: String,
    roborazziOptions: RoborazziOptions = DefaultRoborazziOptions,
) {
    this.onRoot(true)
        .captureRoboImage(
            PATH + "${screenshotName}_$deviceName.png",
            roborazziOptions = roborazziOptions,
        )
}

fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.captureForDevice(
    deviceName: String,
    deviceSpec: String,
    screenshotName: String,
    roborazziOptions: RoborazziOptions = DefaultRoborazziOptions,
    darkMode: Boolean = false,
    body: @Composable () -> Unit,
) {
    val (width, height, dpi) = extractSpecs(deviceSpec)

    // Set qualifiers from specs
    RuntimeEnvironment.setQualifiers("w${width}dp-h${height}dp-${dpi}dpi")

    this.activity.setContent {
        CompositionLocalProvider(
            LocalInspectionMode provides true,
        ) {

            TestHarness(darkMode = darkMode) {
                body()
            }
        }
    }
    this.onRoot()
        .captureRoboImage(
            PATH + "${screenshotName}_$deviceName.png",
            roborazziOptions = roborazziOptions,
        )
}

/**
 * Takes six screenshots combining light/dark and default/Android themes and whether dynamic color
 * is enabled.
 */
fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.captureMultiTheme(
    name: String,
    overrideFileName: String? = null,
    shouldCompareDarkMode: Boolean = true,
//    shouldCompareDynamicColor: Boolean = true,
//    shouldCompareAndroidTheme: Boolean = true,
    content: @Composable (desc: String) -> Unit,
) {
    val darkModeValues = if (shouldCompareDarkMode) listOf(true, false) else listOf(false)
//    val dynamicThemingValues = if (shouldCompareDynamicColor) listOf(true, false) else listOf(false)
//    val androidThemeValues = if (shouldCompareAndroidTheme) listOf(true, false) else listOf(false)

    var darkMode by mutableStateOf(true)

    this.setContent {
        CompositionLocalProvider(
            LocalInspectionMode provides true,
        ) {
            MyApplicationTheme(
                darkTheme = darkMode,
            ) {
                    val description = generateDescription(
                        shouldCompareDarkMode,
                        darkMode,
//                        shouldCompareAndroidTheme,
//                        androidTheme,
//                        shouldCompareDynamicColor,
//                        dynamicTheming,
                    )
                    content(description)
            }
        }
    }

    // Create permutations
    darkModeValues.forEach { isDarkMode ->
        darkMode = isDarkMode
        val darkModeDesc = if (isDarkMode) "dark" else "light"

//        androidThemeValues.forEach { isAndroidTheme ->
//            androidTheme = isAndroidTheme
//            val androidThemeDesc = if (isAndroidTheme) "androidTheme" else "defaultTheme"
//
//            dynamicThemingValues.forEach dynamicTheme@{ isDynamicTheming ->
//                // Skip tests with both Android Theme and Dynamic color as they're incompatible.
//                if (isAndroidTheme && isDynamicTheming) return@dynamicTheme
//
//                dynamicTheming = isDynamicTheming
//                val dynamicThemingDesc = if (isDynamicTheming) "dynamic" else "notDynamic"

                val filename = overrideFileName ?: name

                this.onRoot()
                    .captureRoboImage(
                        PATH +
                            "$name/$filename" +
                            "_$darkModeDesc" +
//                            "_$androidThemeDesc" +
//                            "_$dynamicThemingDesc" +
                            ".png",
                        roborazziOptions = DefaultRoborazziOptions,
                    )
//            }
//        }
    }
}

@Composable
private fun generateDescription(
    shouldCompareDarkMode: Boolean,
    darkMode: Boolean,
): String {
    val description = "" +
        if (shouldCompareDarkMode) {
            if (darkMode) "Dark" else "Light"
        } else {
            ""
        }
//    +
//        if (shouldCompareAndroidTheme) {
//            if (androidTheme) " Android" else " Default"
//        } else {
//            ""
//        } +
//        if (shouldCompareDynamicColor) {
//            if (dynamicTheming) " Dynamic" else ""
//        } else {
//            ""
//        }

    return description.trim()
}

/**
 * Extracts some properties from the spec string. Note that this function is not exhaustive.
 */
private fun extractSpecs(deviceSpec: String): TestDeviceSpecs {
    val specs = deviceSpec.substringAfter("spec:")
        .split(",").map { it.split("=") }.associate { it[0] to it[1] }
    val width = specs["width"]?.toInt() ?: 360
    val height = specs["height"]?.toInt() ?: 720
    val dpi = specs["dpi"]?.toInt() ?: 480
    return TestDeviceSpecs(width, height, dpi)
}

data class TestDeviceSpecs(val width: Int, val height: Int, val dpi: Int)