package io.lunarlogic.aircasting.exceptions

import java.lang.Exception

abstract class BaseException(private val mCause: Exception? = null, private val mMessageToDisplay: String? = null): Exception(mCause) {
    val messageToDisplay: String? get() = mMessageToDisplay
}

class BluetoothNotSupportedException():
    BaseException(null, "Bluetooth is not supported on this device")

class AudioReaderError(errorCode: Int):
    BaseException(Exception("Audio reader error code: ${errorCode}"), "Unexpected error while reading audio")

class UnknownError(cause: Exception):
    BaseException(cause, "Something went wrong, please contact our support")

class AirBeam2ConnectionOpenFailed(cause: Exception):
    BaseException(cause, "AirBeam connection failed. Please try again.")

class AirBeam2ConfiguringFailed(cause: Exception):
    BaseException(cause)

class AirBeam2ConnectionCloseFailed(cause: Exception):
    BaseException(cause)

class BLENotSupported():
    BaseException(null, "BLE is not supported by this device.")

class AirBeam3ConfiguringFailed(type: String, status: Int):
    BaseException(null, "Configuration of $type failed with status code: $status.")

class InternalAPIError():
    BaseException(null, "Something went wrong, please contact our support.")

class SyncError(t: Throwable? = null):
    BaseException(Exception(t), "Session sync failed, check your network connection.")

class SessionExportFailedError(t: Throwable? = null):
        BaseException(Exception(t), "Something wrong happened during exporting session data.")

class DownloadMeasurementsError(t: Throwable? = null):
    BaseException(Exception(t))

class UnexpectedAPIError(t: Throwable? = null):
    BaseException(Exception(t), "Something went wrong, please contact our support.")

class ChooseAirBeamLocationSelectingPlaceError(t: Throwable? = null):
    BaseException(Exception(t))

class AirBeamResponseParsingError(line: String, t: Throwable? = null):
    BaseException(Exception(t), "Error while parsing line: '$line'.")

class DBInsertException(t: Throwable? = null):
    BaseException(Exception(t), "Trying to insert or update session data after the DB has been cleaned.")

class MissingDeviceAfterConnectionError: BaseException()

class MeasurementsFromSDCardParsingError(cause: Exception):
    BaseException(cause, "There was a problem while parsing measurements from SD card.")
