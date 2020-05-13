package io.lunarlogic.aircasting.exceptions

import java.lang.Exception

abstract class BaseException(private val mCause: Exception?, private val mMessageToDisplay: String?): Exception(mCause) {
    val messageToDisplay: String? get() = mMessageToDisplay
}

class BluetoothNotSupportedException():
    BaseException(null, "Bluetooth is not supported on this device")

class UnknownError(cause: Exception):
    BaseException(cause, "Something went wrong, please contact our support")

class AirBeam2ConnectionOpenFailed(cause: Exception):
    BaseException(cause, "AirBeam2 connection failed. If you agreed on pairing and still see this error, please contact support")

class SensorResponseParsingError(cause: Exception?):
    BaseException(cause, "There was a problem while parsing response from the sensor. Please make sure that you follow the required protocol.")

class AirBeam2ConnectionCloseFailed(cause: Exception):
    BaseException(cause, null)

class InternalAPIError():
    BaseException(null, "Something went wrong, please contact our support.")

class UnexpectedAPIError(t: Throwable):
    BaseException(Exception(t), "Something went wrong, please contact our support.")