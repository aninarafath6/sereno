plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("plugin.serialization") version "1.9.24"
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.sereno"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.sereno"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    viewBinding {
        enable = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(platform("io.github.jan-tennert.supabase:bom:3.1.1"))
    implementation(libs.postgrest.kt)
    implementation("io.ktor:ktor-client-android:3.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.kotlinxCoroutines}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${libs.versions.kotlinxCoroutines}")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.moshi:moshi:1.12.0")
    implementation("io.github.jan-tennert.supabase:serializer-moshi:3.0.3")

    implementation("com.squareup.moshi:moshi:1.15.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")



    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.android)
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.room:room-rxjava2:2.6.1")
    implementation("androidx.room:room-rxjava3:2.6.1")
    implementation("androidx.room:room-guava:2.6.1")
    testImplementation("androidx.room:room-testing:2.5.2")

    implementation ("com.airbnb.android:lottie:3.5.0")
    implementation ("androidx.work:work-runtime:2.7.0")
    implementation("androidx.work:work-runtime-ktx:2.3.1")


}