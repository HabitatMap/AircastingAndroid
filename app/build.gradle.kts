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

    testOptions {
        animationsDisabled = true
        unitTests.isReturnDefaultValues = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions.jvmTarget = "11"
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
    testImplementation(AppDependencies.testImplementation)
    debugImplementation(AppDependencies.debugImplementation)
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.9.0")
}

