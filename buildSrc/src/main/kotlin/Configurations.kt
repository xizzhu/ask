/*
 * Copyright (C) 2019 Xizhi Zhu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.gradle.api.JavaVersion

object Versions {
    object App {
        const val code = 100
        const val name: String = "${code / 10000}.${(code % 10000) / 100}.${code % 100}"
    }

    object Coveralls {
        const val classpath = "2.8.3"
    }

    object Sdk {
        const val classpath = "3.5.2"
        const val buildTools = "29.0.2"
        const val compile = 29
        const val min = 21
        const val target = 29
    }

    val java = JavaVersion.VERSION_1_8

    object Kotlin {
        const val core = "1.3.60"
        const val coroutines = "1.3.2"
    }

    object AndroidX {
        object Test {
            const val junit = "1.1.1"
            const val rules = "1.2.0"
        }
    }
}

object Dependencies {
    object Sdk {
        const val classpath = "com.android.tools.build:gradle:${Versions.Sdk.classpath}"
    }

    object Coveralls {
        const val classpath = "org.kt3k.gradle.plugin:coveralls-gradle-plugin:${Versions.Coveralls.classpath}"
    }

    object Kotlin {
        const val classpath = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.Kotlin.core}"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.Kotlin.core}"
        const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.Kotlin.coroutines}"
        const val test = "org.jetbrains.kotlin:kotlin-test-junit:${Versions.Kotlin.core}"
        const val coroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.Kotlin.coroutines}"
    }

    object AndroidX {
        object Test {
            const val junit = "androidx.test.ext:junit:${Versions.AndroidX.Test.junit}"
            const val rules = "androidx.test:rules:${Versions.AndroidX.Test.rules}"

            const val runner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }
}
