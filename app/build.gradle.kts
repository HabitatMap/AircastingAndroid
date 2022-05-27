plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("androidx.navigation.safeargs")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
}

android {
    compileSdk = 32
    buildToolsVersion = "30.0.3"
    defaultConfig {
        applicationId = "pl.llp.aircasting"
        minSdk = 21
        targetSdk = 32
        versionCode = 197
        versionName = "2.0.49"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // resConfigs = ["en", "es", "fr"]

        // Read the API key from ./secure.properties into R.string.maps_api_key
        /*val secureProps = Properties()
        if (file("../secure.properties").exists()) file("../secure.properties").inputStream()*/

    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        dataBinding = true
    }

    sourceSets {
        this.getByName("main") {
            this.java.apply {
                srcDir("src/main/assets")
                srcDir("src/main/res")
            }
        }
        this.getByName("test") {
            this.java.srcDir("src/test/res")
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.21")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    val androidXVersion = "1.4.1"
    implementation("androidx.appcompat:appcompat:$androidXVersion")
    implementation("androidx.fragment:fragment-ktx:$androidXVersion")
    implementation("androidx.activity:activity-ktx:1.4.0")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.4.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.4.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    val lifecycleVersion = "2.4.1"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")

    implementation("androidx.recyclerview:recyclerview:1.2.1")
    // for complex recycler views
    implementation("com.xwray:groupie:2.8.1")
    implementation("com.xwray:groupie-kotlin-android-extensions:2.8.1")

    implementation("com.google.android.material:material:1.6.0")

    implementation("com.google.guava:guava:29.0-android")
    implementation("org.greenrobot:eventbus:3.2.0")

    // Process phoenix - restarting app library
    implementation("com.jakewharton:process-phoenix:2.0.0")

    val daggerVersion = "2.41"
    implementation("com.google.dagger:dagger:$daggerVersion")
    kapt("com.google.dagger:dagger-compiler:$daggerVersion")
    kaptAndroidTest("com.google.dagger:dagger-compiler:$daggerVersion")

    // for http requests
    val retrofit_version = "2.9.0"
    implementation("com.squareup.retrofit2:retrofit:$retrofit_version")
    androidTestImplementation("com.squareup.retrofit2:retrofit-mock:$retrofit_version")
    implementation("com.squareup.retrofit2:converter-gson:$retrofit_version")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.6")
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:4.9.3")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.opencsv:opencsv:4.6")

    implementation("com.google.android.gms:play-services-location:19.0.1")
    implementation("com.google.android.gms:play-services-maps:18.0.2")

    // for Google Places API
    implementation("com.google.android.libraries.places:places:2.6.0")
    implementation("com.google.android.gms:play-services-gcm:17.0.0")
    implementation("com.google.auto.value:auto-value-annotations:1.9")

    // for Firebase Crashlytics and Test Lab
    implementation(platform("com.google.firebase:firebase-bom:30.0.2"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    val roomVersion = "2.4.2"
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    implementation("commons-codec:commons-codec:1.14")
    implementation("no.nordicsemi.android:ble:2.2.4")

    // charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // for testing
    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.8.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.6.20")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation("org.robolectric:robolectric:4.6")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.9.3")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.4.0")
    implementation("androidx.test.espresso:espresso-idling-resource:3.4.0")

    // for mocking in tests
    val mockitoVersion = "4.0.0"
    androidTestImplementation("org.mockito:mockito-core:$mockitoVersion")
    androidTestImplementation("org.mockito:mockito-android:$mockitoVersion")
    androidTestImplementation("com.nhaarman:mockito-kotlin:1.5.0")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
}

