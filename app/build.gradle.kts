plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
//    id("com.gradle.enterprise") version "3.13.4"
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.zebrand.app1food30s"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.zebrand.app1food30s"
        minSdk = 28
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
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("com.google.firebase:firebase-database-ktx:20.3.1")
    implementation("com.google.firebase:firebase-auth-ktx:22.3.1")
    implementation("androidx.room:room-common:2.6.1")
    implementation("androidx.activity:activity-ktx:1.8.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    // =============== Circled avatar
    implementation("com.google.android.material:material:1.12.0-rc01")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    // For control over item selection of both touch and mouse driven selection
    implementation("androidx.recyclerview:recyclerview-selection:1.1.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.airbnb.android:lottie:6.3.0")
    implementation("com.github.vipulasri:timelineview:1.1.5")
    implementation("com.borjabravo:readmoretextview:2.1.0")

    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation("com.google.firebase:firebase-storage-ktx:20.3.0")
    implementation("com.firebaseui:firebase-ui-storage:7.2.0")

    implementation("androidx.fragment:fragment-ktx:1.6.2")
    // =============== JSON
    implementation("com.google.code.gson:gson:2.10.1")
    // ============== Download and display image from URL
    implementation("com.squareup.picasso:picasso:2.8")
    // =============== Shimmer effect
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("com.github.sharish:ShimmerRecyclerView:v1.3")
    implementation("com.todkars:shimmer-recyclerview:0.4.1")
    // =============== Pull to request
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")

    // ================ Firebase services =======================
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-firestore:24.11.0")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-auth")

    // ================ ROOM Database =================
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    // ================ Map =================
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.libraries.places:places:3.4.0")
    implementation("com.github.prolificinteractive:material-calendarview:2.0.0")
//    implementation("org.threeten:threetenbp:1.5.1")
//    implementation("com.android.support:multidex:2.0.1")
}