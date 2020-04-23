package io.lunarlogic.aircasting.exceptions

import java.lang.Exception

abstract class BaseException(message: String?, private val mMessageToDisplay: String): Exception(message) {
    val messageToDisplay: String get() = mMessageToDisplay
}

class BluetoothNotSupportedException():
    BaseException(null, "Bluetooth is not supported on this device")

class BluetoothRequiredException():
    BaseException(null, "You need to turn bluetooth on to continue")