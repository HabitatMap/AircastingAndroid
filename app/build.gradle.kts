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
    compileSdk = AppConfig.compileSdk
    buildToolsVersion = AppConfig.buildToolsVersion
    defaultConfig {
        applicationId = "pl.llp.aircasting"
        minSdk = AppConfig.minSdk
        targetSdk = AppConfig.targetSdk
        versionCode = AppConfig.versionCode
        versionName = AppConfig.versionName

        testInstrumentationRunner = AppConfig.androidTestInstrumentation
        resConfigs("en", "es, fr")

        val secureProps = project.properties
        if (file("../secure.properties").exists()) file("../secure.properties").inputStream().use {
            secureProps.apply { it }
        }
    }

    buildTypes {
        getByName("debug") {
            manifestPlaceholders["crashlyticsCollectionEnabled"] = false
        }

        getByName("release") {
            isMinifyEnabled = false
            manifestPlaceholders["crashlyticsCollectionEnabled"] = true
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

    lint {
        abortOnError = false
    }

    buildFeatures {
        dataBinding = true
    }

    packagingOptions {
        resources.excludes.add("META-INF/notice.txt")
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
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}")
    implementation("androidx.legacy:legacy-support-v4:${Versions.legacySupport}")

    implementation("androidx.appcompat:appcompat:${Versions.androidX}")
    implementation("androidx.fragment:fragment-ktx:${Versions.androidX}")
    implementation("androidx.activity:activity-ktx:${Versions.activityKtx}")
    implementation("androidx.core:core-ktx:${Versions.coreKtx}")
    implementation("androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}")
    implementation("androidx.lifecycle:lifecycle-extensions:${Versions.lifeCycleExtension}")
    implementation("androidx.navigation:navigation-fragment-ktx:${Versions.fragmentKtx}")
    implementation("androidx.navigation:navigation-ui-ktx:${Versions.fragmentKtx}")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:${Versions.swipeRefresh}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifeCycle}")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifeCycle}")
    implementation("androidx.recyclerview:recyclerview:${Versions.recyclerView}")

    implementation("com.google.android.material:material:${Versions.material}")
    implementation("com.xwray:groupie:${Versions.groupie}")
    implementation("com.xwray:groupie-kotlin-android-extensions:${Versions.groupie}")

    implementation("com.google.guava:guava:${Versions.guava}")
    implementation("org.greenrobot:eventbus:${Versions.eventBus}")

    implementation("com.jakewharton:process-phoenix:2.0.0")

    implementation("com.google.dagger:dagger:${Versions.dagger}")
    kapt("com.google.dagger:dagger-compiler:${Versions.dagger}")
    kaptAndroidTest("com.google.dagger:dagger-compiler:${Versions.dagger}")

    implementation("com.squareup.retrofit2:retrofit:${Versions.retrofit}")
    androidTestImplementation("com.squareup.retrofit2:retrofit-mock:${Versions.retrofit}")
    implementation("com.squareup.retrofit2:converter-gson:${Versions.retrofit}")
    implementation("com.squareup.okhttp3:logging-interceptor:${Versions.loggingInterceptor}")
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:${Versions.mockServerWeb}")
    implementation("com.google.code.gson:gson:${Versions.gson}")
    implementation("com.opencsv:opencsv:${Versions.openCv}")

    implementation("com.google.android.gms:play-services-location:${Versions.playServicesLocation}")
    implementation("com.google.android.gms:play-services-maps:${Versions.playServicesMaps}")

    implementation("com.google.android.libraries.places:places:${Versions.places}")
    implementation("com.google.android.gms:play-services-gcm:${Versions.playServicesGCM}")
    implementation("com.google.auto.value:auto-value-annotations:${Versions.autoAnnotations}")

    implementation(platform("com.google.firebase:firebase-bom:${Versions.firebaseBom}"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    implementation("androidx.room:room-runtime:${Versions.roomDB}")
    kapt("androidx.room:room-compiler:${Versions.roomDB}")

    implementation("commons-codec:commons-codec:${Versions.commonsCodec}")
    implementation("no.nordicsemi.android:ble:${Versions.androidBle}")

    implementation("com.github.PhilJay:MPAndroidChart:${Versions.mpAndroidChart}")

    testImplementation("junit:junit:${Versions.junit}")
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

