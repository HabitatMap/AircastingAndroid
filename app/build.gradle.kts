import java.util.Properties

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


val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}
val keystorePath: String? = localProperties.getProperty("keystore.path")
val keystorePassword: String? = localProperties.getProperty("keystore.password")
val keystoreAlias: String? = localProperties.getProperty("keystore.alias")
val keyPasswordFromFile: String? = localProperties.getProperty("key.password")

android {
    compileSdk = AppConfig.compileSdk
    buildToolsVersion = AppConfig.buildToolsVersion

    signingConfigs {
        getByName("debug") {
            storeFile = keystorePath?.let { file(it) }
            storePassword = keystorePassword
            keyAlias = keystoreAlias
            keyPassword = keyPasswordFromFile
        }
        create("release") {
            storeFile = keystorePath?.let { file(it) }
            storePassword = keystorePassword
            keyAlias = keystoreAlias
            keyPassword = keyPasswordFromFile
        }
    }

    defaultConfig {
        testInstrumentationRunnerArguments += mapOf()
        applicationId = "pl.llp.aircasting"
        minSdk = AppConfig.minSdk
        targetSdk = AppConfig.targetSdk
        versionCode = AppConfig.versionCode
        versionName = AppConfig.versionName
        testInstrumentationRunner = AppConfig.androidTestInstrumentation
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
        resourceConfigurations.addAll(arrayOf("en", "fr", "sp"))
        signingConfig = signingConfigs.getByName("debug")

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }
    }

    buildTypes {
        getByName("debug") {
            manifestPlaceholders["crashlyticsCollectionEnabled"] = false
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("release") {
            isMinifyEnabled = false
            manifestPlaceholders["crashlyticsCollectionEnabled"] = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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
        getByName("androidTest") {
            manifest.srcFile("src/androidTest/manifests/AndroidManifest.xml")
        }
    }

    testOptions {
        animationsDisabled = true
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
        execution = "ANDROID_TEST_ORCHESTRATOR"
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
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.4.0")
    kapt(AppDependencies.kaptLibraries)
    kaptAndroidTest(AppDependencies.kaptAndroidTestLibraries)
    kaptTest(AppDependencies.kaptTestLibraries)
    androidTestImplementation(AppDependencies.androidTestImplementation)
    testImplementation(AppDependencies.testImplementation)
    debugImplementation(AppDependencies.debugImplementation)
    androidTestUtil(AppDependencies.androidTestUtil)
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.9.0")
}