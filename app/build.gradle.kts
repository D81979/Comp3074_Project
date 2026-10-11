plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
}

android {
    namespace = "ca.gbc.comp3074.scavengerhuntgroup95project"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "ca.gbc.comp3074.scavengerhuntgroup95project"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    // Store and retrieve locations using Room.
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)

    // Generate the database implementation.
    ksp(libs.androidx.room.compiler)
}