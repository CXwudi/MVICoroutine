@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
  alias(libs.plugins.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.compose)
  alias(libs.plugins.android.application)
}

kotlin {
  jvmToolchain(17)

  androidTarget()
  jvm()
  js {
    browser()
    binaries.executable()
  }
  wasmJs {
    browser()
    binaries.executable()
  }
  listOf(
    iosX64(),
    iosArm64(),
    iosSimulatorArm64()
  ).forEach {
    it.binaries.framework {
      baseName = "ComposeApp"
      isStatic = true
    }
  }

  sourceSets {
    commonMain.dependencies {
      implementation(compose.runtime)
      implementation(compose.ui)
      implementation(compose.foundation)
      implementation(compose.material3)
      implementation(project(":mvicoroutine"))
    }

    androidMain.dependencies {
      implementation(libs.androidx.activityCompose)
    }

    jvmMain.dependencies {
      implementation(compose.desktop.currentOs)
    }

    webMain.dependencies {
      implementation(project.dependencies.platform(libs.kotlin.wrappers.bom))
      implementation("org.jetbrains.kotlin-wrappers:kotlin-browser")
    }

  }
}

android {
  namespace = "sample.app"
  compileSdk = 36

  defaultConfig {
    minSdk = 23
    targetSdk = 36

    applicationId = "sample.app.androidApp"
    versionCode = 1
    versionName = "1.0.0"
  }
}

compose.desktop {
  application {
    mainClass = "MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "sample"
      packageVersion = "1.0.0"
    }
  }
}
