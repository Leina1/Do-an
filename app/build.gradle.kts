plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.apptradeup"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.apptradeup"
        minSdk = 29
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Firebase
    // Sử dụng Firebase BOM để tự động quản lý phiên bản các thư viện Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))

    // Firebase Authentication và các thư viện Firebase khác
    implementation("com.google.firebase:firebase-auth:21.0.1")
    implementation("com.google.firebase:firebase-core:21.0.0")
    implementation("com.google.firebase:firebase-firestore:24.0.0")
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-messaging:23.0.8")



    // ... other dependencies
    // Google Auth
    implementation("com.google.android.gms:play-services-auth:20.6.0") // Đăng nhập bằng Google
    implementation("androidx.credentials:credentials:1.0.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.0.0")
    implementation("com.google.firebase:firebase-storage:20.2.2")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    //UI
    // Các thư viện khác trong dự án
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.picasso)


    // Cloudinary Upload qua HTTP
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    implementation("com.google.android.gms:play-services-location:21.0.1")

    implementation ("org.osmdroid:osmdroid-android:6.1.11")

    implementation ("com.google.code.gson:gson:2.10.1")
    implementation(fileTree(mapOf(
        "dir" to "H:\\AndroiStudiod\\ZaloPayLib",
        "include" to listOf("*.aar", "*.jar"),
        "exclude" to listOf("")
    )))
    // Dependencies cho test
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("commons-codec:commons-codec:1.14")
}