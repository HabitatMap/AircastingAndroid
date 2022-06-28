package pl.llp.aircasting.util

import android.os.Build

fun isSDKGreaterOrEqualToS(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}

fun isSDKGreaterOrEqualToM(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
}

fun isSDKGreaterOrEqualToQ(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
}

fun isSDKGreaterOrEqualToO(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
}

fun isSDKLessOrEqualToNMR1(): Boolean {
    return Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1
}

fun isSDKLessThanN(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.N
}

fun isSDKLessThanM(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
}
