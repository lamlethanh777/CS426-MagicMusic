plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)

    // for @Parcelize
    id("kotlin-parcelize")

    // kapt for Room Database
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.example.cs426_magicmusic"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.cs426_magicmusic"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.lifecycle.process)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //Room Database Library
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-ktx:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    // To use Kotlin annotation processing tool (kapt)
    kapt("androidx.room:room-compiler:$roomVersion")

    // ViewModel and Lifecycle
    val lifecycleVersion = "2.8.4"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")

    val glideVersion = "4.16.0"
    // Image loading
    implementation("com.github.bumptech.glide:glide:$glideVersion")

    val easyPermissionKtxVersion = "1.0.0"
    implementation("com.vmadalin:easypermissions-ktx:$easyPermissionKtxVersion")
    val fragmentVersion = "1.8.3"
    implementation("androidx.fragment:fragment-ktx:$fragmentVersion")

    val swipeRefreshLayoutVersion = "1.1.0"
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:$swipeRefreshLayoutVersion")

    //for okhttp - deploy AI
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")

    //for viewModelScope
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")

    //ProSoundEQ for Equalizer
    implementation("com.github.khaouitiabdelhakim:ProSoundEQ:1.1.0")

    val exoPlayerVersion = "1.4.1"
    implementation("androidx.media3:media3-exoplayer:$exoPlayerVersion")
    implementation("androidx.media3:media3-ui:$exoPlayerVersion")
}