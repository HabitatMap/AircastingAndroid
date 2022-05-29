import org.gradle.api.artifacts.dsl.DependencyHandler

object AppDependencies {
    private const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    private const val legacySupport = "androidx.legacy:legacy-support-v4:${Versions.legacySupport}"
    private const val coroutinesCore = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    private const val coroutinesAndroid =
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"

    /* AndroidX */
    private const val appCompat = "androidx.appcompat:appcompat:${Versions.androidX}"
    private const val fragmentKTX = "androidx.fragment:fragment-ktx:${Versions.androidX}"
    private const val activityKTX = "androidx.activity:activity-ktx:${Versions.activityKtx}"
    private const val coreKTX = "androidx.core:core-ktx:${Versions.coreKtx}"
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
    private const val groupie = "com.xwray:groupie:${Versions.groupie}"
    private const val groupieAndroidExtension =
        "com.xwray:groupie-kotlin-android-extensions:${Versions.groupie}"
    private const val guava = "com.google.guava:guava:${Versions.guava}"
    private const val eventBus = "org.greenrobot:eventbus:${Versions.eventBus}"
    private const val processPhoenix = "com.jakewharton:process-phoenix:2.0.0"
    private const val dagger = "com.google.dagger:dagger:${Versions.dagger}"
    private const val daggerCompiler = "com.google.dagger:dagger-compiler:${Versions.dagger}"
    private const val daggerCompilerTest = "com.google.dagger:dagger-compiler:${Versions.dagger}"

    /* Networking */
    private const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    private const val retrofitMock = "com.squareup.retrofit2:retrofit-mock:${Versions.retrofit}"
    private const val converterGSON = "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"
    private const val logginIntercepter =
        "com.squareup.okhttp3:logging-interceptor:${Versions.loggingInterceptor}"
    private const val okHttp3MockWebServerTest =
        "com.squareup.okhttp3:mockwebserver:${Versions.mockServerWeb}"
    private const val gson = "com.google.code.gson:gson:${Versions.gson}"
    private const val openCV = "com.opencsv:opencsv:${Versions.openCv}"

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

    private const val firebaseBom = "com.google.firebase:firebase-bom:${Versions.firebaseBom}"
    private const val crashlytics = "com.google.firebase:firebase-crashlytics-ktx"
    private const val analyticsKTX = "com.google.firebase:firebase-analytics-ktx"

    /* Room DB */
    private const val roomDB = "androidx.room:room-runtime:${Versions.roomDB}"
    private const val roomDBCompiler = "androidx.room:room-compiler:${Versions.roomDB}"
    private const val commonsCodec = "commons-codec:commons-codec:${Versions.commonsCodec}"
    private const val androidBle = "no.nordicsemi.android:ble:${Versions.androidBle}"
    private const val mpAndroidChart =
        "com.github.PhilJay:MPAndroidChart:${Versions.mpAndroidChart}"

    val appLibraries = arrayListOf<String>().apply {
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
        add(constraintLayout)
        add(swipRefreshLayout)
        add(recycleView)
        add(materialDesign)
        add(groupie)
        add(groupieAndroidExtension)
        add(guava)
        add(eventBus)
        add(processPhoenix)
        add(dagger)
        add(retrofit)
        add(retrofitMock)
        add(converterGSON)
        add(logginIntercepter)
        add(okHttp3MockWebServerTest)
        add(gson)
        add(openCV)
        add(playServicesLocation)
        add(playServicesMaps)
        add(mapsPlaces)
        add(playServicesGCM)
        add(autoAnnotations)
        add(firebaseBom)
        add(crashlytics)
        add(analyticsKTX)
        add(roomDB)
        add(commonsCodec)
        add(androidBle)
        add(mpAndroidChart)
    }
    val kaptLibraries = arrayListOf<String>().apply {
        add(daggerCompiler)
        add(roomDB)
        add(roomDBCompiler)
    }
    val kaptTestLibraries = arrayListOf<String>().apply {
        add(daggerCompilerTest)
    }
}

fun DependencyHandler.kapt(list: List<String>) {
    list.forEach { dependency ->
        add("kapt", dependency)
    }
}

fun DependencyHandler.implementation(list: List<String>) {
    list.forEach { dependency ->
        add("implementation", dependency)
    }
}

