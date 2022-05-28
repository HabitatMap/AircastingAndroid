import org.gradle.api.artifacts.dsl.DependencyHandler

object AppDependencies {
    private val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    private val legacySupport = "androidx.legacy:legacy-support-v4:${Versions.legacySupport}"
    private val coroutinesCore = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    private val coroutinesAndroid =
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"

    /* AndroidX */
    private val appCompat = "androidx.appcompat:appcompat:${Versions.androidX}"
    private val fragmentKTX = "androidx.fragment:fragment-ktx:${Versions.androidX}"
    private val activityKTX = "androidx.activity:activity-ktx:${Versions.activityKtx}"
    private val coreKTX = "androidx.core:core-ktx:${Versions.coreKtx}"
    private val navigationFragmentKTX =
        "androidx.navigation:navigation-fragment-ktx:${Versions.fragmentKtx}"
    private val navigationUiKTX = "androidx.navigation:navigation-ui-ktx:${Versions.fragmentKtx}"
    private val lifeCycleViewModelKTX =
        "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifeCycle}"
    private val lifeCycleLiveDataKTX =
        "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifeCycle}"
    private val constraintLayout =
        "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
    private val swipRefreshLayout =
        "androidx.swiperefreshlayout:swiperefreshlayout:${Versions.swipeRefresh}"
    private val lifeCycleExtension =
        "androidx.lifecycle:lifecycle-extensions:${Versions.lifeCycleExtension}"
    private val recycleView = "androidx.recyclerview:recyclerview:${Versions.recyclerView}"

    private val materialDesign = "com.google.android.material:material:${Versions.material}"






    //test libs
    private val junit = "junit:junit:${Versions.junit}"
    private val extJUnit = "androidx.test.ext:junit:${Versions.extJunit}"
    private val espressoCore = "androidx.test.espresso:espresso-core:${Versions.espresso}"

    val appLibraries = arrayListOf<String>().apply {
        add(kotlinStdLib)
        add(coreKtx)
        add(constraintLayout)
    }

    val androidTestLibraries = arrayListOf<String>().apply {
        add(extJUnit)
        add(espressoCore)
    }

    val testLibraries = arrayListOf<String>().apply {
        add(junit)
    }
}

//util functions for adding the different type dependencies from build.gradle file
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