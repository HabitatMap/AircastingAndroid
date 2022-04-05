package pl.llp.aircasting.lib

import android.os.Build

fun isSDKVersionBiggerThanS(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}

fun isSDKVersionBiggerThanM(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
}

fun isSDKVersionBiggerThanQ(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
}

fun isAPIVersionBiggerThanO(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
}

fun isAPIVersionLessThanNMR1(): Boolean {
    return Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1
}

fun isAPIVersionLessThanN(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.N
}

fun isAPIVersionLessThanM(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
}
