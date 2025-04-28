import org.gradle.api.artifacts.dsl.DependencyHandler

object AppDependencies {
    private const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    private const val legacySupport = "androidx.legacy:legacy-support-v4:${Versions.legacySupport}"
    private const val coroutinesCore = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    private const val coroutinesAndroid =
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
    private const val coroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}"

    /* AndroidX */
    private const val appCompat = "androidx.appcompat:appcompat:${Versions.androidX}"
    private const val fragment = "androidx.fragment:fragment:${Versions.androidX}"
    private const val fragmentKTX = "androidx.fragment:fragment-ktx:${Versions.androidX}"
    private const val activityKTX = "androidx.activity:activity-ktx:${Versions.activityKtx}"
    private const val coreKTX = "androidx.core:core-ktx:${Versions.coreKtx}"
    private const val testCoreKTX = "androidx.test:core-ktx:${Versions.testCoreKtx}"
    private const val testExtJunitKTX = "androidx.test.ext:junit-ktx:${Versions.testExtJunit}"
    private const val testExtJunit = "androidx.test.ext:junit:${Versions.testExtJunit}"
    private const val robolectric = "org.robolectric:robolectric:${Versions.robolectric}"
    private const val navigationFragmentKTX =
        "androidx.navigation:navigation-fragment-ktx:${Versions.fragmentKtx}"
    private const val navigationUiKTX =
        "androidx.navigation:navigation-ui-ktx:${Versions.fragmentKtx}"
    private const val lifeCycleExtension =
        "androidx.lifecycle:lifecycle-extensions:${Versions.lifeCycleExtension}"
    private const val lifeCycleViewModelKTX =
        "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifeCycle}"
    private const val lifeCycleLiveDataKTX =
        "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifeCycle}"
    private const val constraintLayout =
        "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
    private const val swipRefreshLayout =
        "androidx.swiperefreshlayout:swiperefreshlayout:${Versions.swipeRefresh}"
    private const val recycleView = "androidx.recyclerview:recyclerview:${Versions.recyclerView}"
    private const val materialDesign = "com.google.android.material:material:${Versions.material}"
    private const val coreSplashScreen = "androidx.core:core-splashscreen:${Versions.coreSplashScreen}"


    private const val groupie = "com.github.lisawray.groupie:groupie:${Versions.groupie}"
    private const val groupieAndroidExtension =
        "com.github.lisawray.groupie:groupie-kotlin-android-extensions:${Versions.groupie}"

    private const val guava = "com.google.guava:guava:${Versions.guava}"
    private const val eventBus = "org.greenrobot:eventbus:${Versions.eventBus}"
    private const val processPhoenix = "com.jakewharton:process-phoenix:2.0.0"

    /* Dagger */
    private const val dagger = "com.google.dagger:dagger:${Versions.dagger}"
    private const val daggerCompiler = "com.google.dagger:dagger-compiler:${Versions.dagger}"
    private const val daggerAndroid = "com.google.dagger:dagger-android:${Versions.dagger}"
    private const val daggerAndroidSupport = "com.google.dagger:dagger-android-support:${Versions.dagger}"
    private const val daggerAndroidProcessor = "com.google.dagger:dagger-android-processor:${Versions.dagger}"

    /* Networking */
    private const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    private const val retrofitMock = "com.squareup.retrofit2:retrofit-mock:${Versions.retrofit}"
    private const val converterGSON = "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"
    private const val loggingInterceptor =
        "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp3}"
    private const val okHttp3MockWebServerTest =
        "com.squareup.okhttp3:mockwebserver:${Versions.okhttp3}"
    private const val gson = "com.google.code.gson:gson:${Versions.gson}"

    private const val openCV = "com.opencsv:opencsv:${Versions.openCv}"

    /* Glide */
    private const val glide = "com.github.bumptech.glide:glide:${Versions.glide}"
    private const val glideCompiler = "com.github.bumptech.glide:compiler:${Versions.glide}"

    /* Google Play */
    private const val playServicesLocation =
        "com.google.android.gms:play-services-location:${Versions.playServicesLocation}"
    private const val playServicesMaps =
        "com.google.android.gms:play-services-maps:${Versions.playServicesMaps}"
    private const val mapsPlaces = "com.google.android.libraries.places:places:${Versions.places}"
    private const val playServicesGCM =
        "com.google.android.gms:play-services-gcm:${Versions.playServicesGCM}"

    private const val autoAnnotations =
        "com.google.auto.value:auto-value-annotations:${Versions.autoAnnotations}"

    /* Firebase */
    const val firebaseBom = "com.google.firebase:firebase-bom:${Versions.firebaseBom}"
    private const val crashlyticsKTX = "com.google.firebase:firebase-crashlytics-ktx"
    private const val analyticsKTX = "com.google.firebase:firebase-analytics-ktx"

    /* Room DB */
    private const val roomKTX = "androidx.room:room-ktx:${Versions.roomDB}"
    private const val roomDB = "androidx.room:room-runtime:${Versions.roomDB}"
    private const val roomDBCompiler = "androidx.room:room-compiler:${Versions.roomDB}"
    private const val roomDBTest = "androidx.room:room-testing:${Versions.roomDB}"

    /* Miscellaneous */
    private const val commonsCodec = "commons-codec:commons-codec:${Versions.commonsCodec}"
    private const val mpAndroidChart =
        "com.github.PhilJay:MPAndroidChart:${Versions.mpAndroidChart}"

    /* Bluetooth Low Energy */
    private const val androidBle = "no.nordicsemi.android:ble:${Versions.androidBle}"
    private const val androidBleKtx = "no.nordicsemi.android:ble-ktx:${Versions.androidBle}"

    /* Camera */
    private const val cameraImagePicker = "com.github.dhaval2404:imagepicker:${Versions.cameraImagePicker}"

    /**
     * Tests
     */
    private const val junit = "junit:junit:${Versions.junit}"
    private const val kotlinJunit = "org.jetbrains.kotlin:kotlin-test-junit:${Versions.kotlinJunit}"
    private const val orchestrator = "androidx.test:orchestrator:${Versions.orchestrator}"
    private const val testRunner = "androidx.test:runner:${Versions.testRunner}}"

    /* Espresso */
    private const val espressoCore = "androidx.test.espresso:espresso-core:${Versions.espresso}"
    private const val espressoIntents =
        "androidx.test.espresso:espresso-intents:${Versions.espresso}"
    private const val espressoContrib =
        "androidx.test.espresso:espresso-contrib:${Versions.espressoContrib}"
    private const val espressoIdlingResource =
        "androidx.test.espresso:espresso-idling-resource:${Versions.espresso}"
    private const val awaitility = "org.awaitility:awaitility:${Versions.awaitility}"

    /* Mockito */
    private const val mockitoCore = "org.mockito:mockito-core:${Versions.mockito}"
    private const val mockitoAndroid = "org.mockito:mockito-android:${Versions.mockito}"
    private const val mockitoKotlin = "org.mockito.kotlin:mockito-kotlin:${Versions.mockitoKotlin}"
    private const val fixture = "com.appmattus.fixture:fixture:${Versions.fixture}"
    private const val turbine = "app.cash.turbine:turbine:${Versions.turbine}"

    private const val mockk = "io.mockk:mockk:${Versions.mockk}"

    /* Debug only */
    private const val leakCanary =
        "com.squareup.leakcanary:leakcanary-android:${Versions.leakCanary}"
    private const val fragmentTesting =
        "androidx.fragment:fragment-testing:${Versions.fragmentTesting}"

    val appLibraries = arrayListOf<String>().apply {
        add(kotlinStdLib)
        add(fragment)
        add(kotlinStdLib)
        add(legacySupport)
        add(coroutinesCore)
        add(coroutinesAndroid)
        add(appCompat)
        add(fragmentKTX)
        add(activityKTX)
        add(coreKTX)
        add(navigationFragmentKTX)
        add(navigationUiKTX)
        add(lifeCycleExtension)
        add(lifeCycleViewModelKTX)
        add(lifeCycleLiveDataKTX)
        add(espressoContrib)
        add(espressoIdlingResource)
        add(constraintLayout)
        add(swipRefreshLayout)
        add(recycleView)
        add(materialDesign)
        add(coreSplashScreen)
        add(groupie)
        add(groupieAndroidExtension)
        add(guava)
        add(eventBus)
        add(processPhoenix)
        add(dagger)
        add(daggerAndroid)
        add(daggerAndroidSupport)
        add(retrofit)
        add(retrofitMock)
        add(converterGSON)
        add(loggingInterceptor)
        add(okHttp3MockWebServerTest)
        add(gson)
        add(openCV)
        add(glide)
        add(playServicesLocation)
        add(playServicesMaps)
        add(mapsPlaces)
        add(playServicesGCM)
        add(autoAnnotations)
        add(crashlyticsKTX)
        add(analyticsKTX)
        add(roomKTX)
        add(roomDB)
        add(commonsCodec)
        add(androidBle)
        add(androidBleKtx)
        add(mpAndroidChart)
        add(cameraImagePicker)
    }
    val kaptLibraries = arrayListOf<String>().apply {
        add(daggerCompiler)
        add(roomDBCompiler)
        add(glideCompiler)
        add(daggerAndroidProcessor)
    }
    val kaptAndroidTestLibraries = arrayListOf<String>().apply {
        add(roomDBCompiler)
        add(roomDBTest)
        add(daggerCompiler)
        add(daggerAndroidProcessor)
    }
    val kaptTestLibraries = arrayListOf<String>().apply {
        add(roomDBCompiler)
        add(roomDBTest)
        add(daggerCompiler)
        add(daggerAndroidProcessor)
    }
    val androidTestImplementation = arrayListOf<String>().apply {
        add(mockitoAndroid)
        add(roomDBCompiler)
        add(roomDBTest)
        add(espressoCore)
        add(mockitoKotlin)
        add(espressoIntents)
        add(testCoreKTX)
        add(testExtJunitKTX)
        add(testExtJunit)
        add(awaitility)
        add(testRunner)
        add(coroutinesTest)
    }
    val testImplementation = arrayListOf<String>().apply {
        add(coroutinesTest)
        add(mockitoCore)
        add(junit)
        add(mockitoKotlin)
        add(fixture)
        add(turbine)
        add(mockk)
        add(kotlinJunit)
        add(robolectric)
    }
    val debugImplementation = arrayListOf<String>().apply {
        add(leakCanary)
        add(fragmentTesting)
    }
    val androidTestUtil = arrayListOf<String>().apply {
        add(orchestrator)
    }
}

fun DependencyHandler.implementation(list: List<String>) {
    list.forEach { dependency ->
        add("implementation", dependency)
    }
}

fun DependencyHandler.kapt(list: List<String>) {
    list.forEach { dependency ->
        add("kapt", dependency)
    }
}

fun DependencyHandler.kaptAndroidTest(list: List<String>) {
    list.forEach { dependency ->
        add("kaptAndroidTest", dependency)
    }
}

fun DependencyHandler.kaptTest(list: List<String>) {
    list.forEach { dependency ->
        add("kaptTest", dependency)
    }
}

fun DependencyHandler.androidTestImplementation(list: List<String>) {
    list.forEach { dependency ->
        add("androidTestImplementation", dependency)
    }
}

fun DependencyHandler.testImplementation(list: List<String>) {
    list.forEach { dependency ->
        add("testImplementation", dependency)
    }
}

fun DependencyHandler.debugImplementation(list: List<String>) {
    list.forEach { dependency ->
        add("debugImplementation", dependency)
    }
}

fun DependencyHandler.androidTestUtil(list: List<String>) {
    list.forEach { dependency ->
        add("androidTestUtil", dependency)
    }
}