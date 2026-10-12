import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.courtneythan.aboutme"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.courtneythan.aboutme"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        // Point this at the raw URL of profile.json in your GitHub repo to update the
        // app's content without shipping a new build. Leave blank to use bundled content only.
        // e.g. "https://raw.githubusercontent.com/<you>/about-me-android/main/app/src/main/assets/profile.json"
        buildConfigField("String", "PROFILE_URL", "\"\"")

        // Pexels API key for the photo section. Keep it OUT of git: put
        //   PEXELS_API_KEY=your_key
        // in local.properties (already git-ignored). Without it the photos section
        // shows a friendly "add a key" card and everything else works.
        val pexelsKey = rootProject.file("local.properties")
            .takeIf { it.exists() }
            ?.readLines()
            ?.firstOrNull { it.startsWith("PEXELS_API_KEY=") }
            ?.substringAfter("=")
            ?.trim()
            .orEmpty()
        buildConfigField("String", "PEXELS_API_KEY", "\"$pexelsKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))
    implementation(project(":feature:profile"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Validates the bundled profile.json on every CI run.
    testImplementation(libs.junit)
    testImplementation(project(":core:model"))
}
