plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("androidx.navigation.safeargs")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
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
        resourceConfigurations.addAll(arrayOf("en", "fr", "sp"))

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildTypes {
        getByName("debug") {
            manifestPlaceholders["crashlyticsCollectionEnabled"] = false
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            manifestPlaceholders["crashlyticsCollectionEnabled"] = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    sourceSets {
        getByName("main") {
            this.java.apply {
                srcDir("src/main/assets")
                srcDir("src/main/res")
            }
        }
        getByName("test") {
            this.java.srcDir("src/test/res")
        }
        getByName("androidTest").assets.srcDirs(files("$projectDir/schemas"))
    }

    kotlinOptions.jvmTarget = "1.8"
    lint.abortOnError = false
    buildFeatures.dataBinding = true
}

configurations.forEach {
    it.apply {
        exclude(module = "commons-logging")
        exclude(module = "httpclient")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(platform(AppDependencies.firebaseBom))
    implementation(AppDependencies.appLibraries)
    kapt(AppDependencies.kaptLibraries)
    kaptAndroidTest(AppDependencies.kaptAndroidTestLibraries)
    androidTestImplementation(AppDependencies.androidTestImplementation)
    // Tests
    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.8.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.6.20")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation("org.robolectric:robolectric:4.8.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.4.0")
    implementation("androidx.test.espresso:espresso-idling-resource:3.4.0")

    // for mocking in tests
    androidTestImplementation("org.mockito:mockito-core:4.6.0")
    androidTestImplementation("org.mockito:mockito-android:4.6.0")
    androidTestImplementation("com.nhaarman:mockito-kotlin:1.6.0")
    testImplementation("org.mockito:mockito-core:4.6.0")
}

