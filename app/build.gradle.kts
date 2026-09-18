plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
}

// Release signing is driven entirely by environment variables / -P properties so
// that no credential ever lives in the repository. When they are absent (local
// dev, CI pull-request builds) the release variant falls back to unsigned rather
// than failing configuration, which is what the original setup did.
val keystorePath: String? = System.getenv("KEYSTORE_PATH")
  ?: project.findProperty("KEYSTORE_PATH") as String?
val keystoreStorePassword: String? = System.getenv("STORE_PASSWORD")
  ?: project.findProperty("STORE_PASSWORD") as String?
val keystoreKeyAlias: String = System.getenv("KEY_ALIAS")
  ?: (project.findProperty("KEY_ALIAS") as String? ?: "upload")
val keystoreKeyPassword: String? = System.getenv("KEY_PASSWORD")
  ?: project.findProperty("KEY_PASSWORD") as String?

val releaseSigningAvailable: Boolean =
  keystorePath != null &&
    keystoreStorePassword != null &&
    keystoreKeyPassword != null &&
    file(keystorePath).exists()

android {
  namespace = "com.jaxia.app"
  // Plain API 36. The AI Studio export pinned minor API level 36.1 (Android 16
  // QPR1), which needs a separate SDK platform that is not on CI images and
  // buys nothing here: the app uses no 36.1-only API.
  compileSdk = 36

  defaultConfig {
    applicationId = "com.jaxia.app"
    // Android 8.0. Adaptive icons (mipmap-anydpi-v26) require API 26; below it
    // the launcher falls back to raster mipmaps, which still carried the
    // Android Studio robot. API 24-25 is under ~1% of active devices in 2026,
    // so raising the floor removes the stale icon instead of shipping it.
    minSdk = 26
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    if (releaseSigningAvailable) {
      create("release") {
        storeFile = file(keystorePath!!)
        storePassword = keystoreStorePassword
        keyAlias = keystoreKeyAlias
        keyPassword = keystoreKeyPassword
        enableV1Signing = false
        enableV2Signing = true
        enableV3Signing = true
      }
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig =
        if (releaseSigningAvailable) signingConfigs.getByName("release") else null
    }
    debug {
      applicationIdSuffix = ".debug"
      versionNameSuffix = "-debug"
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  buildFeatures {
    compose = true
    buildConfig = true
  }

  packaging {
    resources {
      excludes += setOf(
        "/META-INF/{AL2.0,LGPL2.1}",
        "/META-INF/DEPENDENCIES",
        "/META-INF/LICENSE*",
        "/META-INF/NOTICE*",
      )
    }
  }

  testOptions {
    unitTests {
      isIncludeAndroidResources = true
      all {
        // Without this, a failing CI build prints only "There were failing
        // tests. See the report at file:///..." — a local path nobody can open
        // from the log. Print the failures inline instead.
        it.testLogging {
          events("failed", "skipped")
          exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
          showStackTraces = true
          showCauses = true
        }
      }
    }
  }

  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

// Room exports its schema here so migrations can be written and verified (AUDITORIA.md B-06/M-05).
ksp { arg("room.schemaLocation", "$projectDir/schemas") }

kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }

// JAXIA is fully offline: no network stack, no Firebase, no image loading from
// URLs. Dependencies that were declared but never referenced in any source file
// (firebase-ai, firebase-appcheck, retrofit, okhttp, moshi, coil) were removed —
// see AUDITORIA.md §A-01. They remain available in gradle/libs.versions.toml if
// a future feature needs them.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)

  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  testImplementation(libs.androidx.room.testing)

  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)

  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)

  "ksp"(libs.androidx.room.compiler)
}
