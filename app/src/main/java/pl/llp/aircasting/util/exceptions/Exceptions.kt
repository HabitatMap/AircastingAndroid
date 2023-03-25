package pl.llp.aircasting.util.exceptions

abstract class BaseException(
    private val mCause: Exception? = null,
    private val mMessageToDisplay: String? = null
) : Exception(mCause) {
    val messageToDisplay: String? get() = mMessageToDisplay
}

class BluetoothNotSupportedException :
    BaseException(null, "Bluetooth is not supported on this device")

class AudioReaderError(errorCode: Int) :
    BaseException(
        Exception("Audio reader error code: ${errorCode}"),
        "Unexpected error while reading audio"
    )

class UnknownError(cause: Exception) :
    BaseException(cause, "Something went wrong, please contact our support")

class NullError(something: String) :
        BaseException(null, "$something was null")

class AirBeamConnectionOpenFailed(cause: Exception) :
    BaseException(cause)

class AirBeam2ConfiguringFailed(cause: Exception) :
    BaseException(cause)

class AirBeam2ConnectionCloseFailed(cause: Exception) :
    BaseException(cause)

class BLENotSupported :
    BaseException(null, "BLE is not supported by this device.")

class AirBeam3ConfiguringFailed(type: String, status: Int) :
    BaseException(null, "Configuration of $type failed with status code: $status.")

class InternalAPIError :
    BaseException(null, "Something went wrong, please contact our support.")

class UnauthorizedError : BaseException(null, "Unauthorized")

class SyncError(t: Throwable? = null) :
    BaseException(Exception(t), "Session sync failed, check your network connection.")

class SessionExportFailedError(t: Throwable? = null) :
    BaseException(Exception(t), "Something wrong happened during exporting session data.")

class SessionUploadPendingError(t: Throwable? = null) :
    BaseException(Exception(t), "Session upload pending - wait a couple of minutes and try again.")

class DownloadMeasurementsError(t: Throwable? = null) :
    BaseException(Exception(t))

class UnexpectedAPIError(t: Throwable? = null) :
    BaseException(Exception(t), "Something went wrong, please contact our support.")

class ChooseAirBeamLocationSelectingPlaceError(t: Throwable? = null) :
    BaseException(Exception(t))

class AirBeamResponseParsingError(line: String, t: Throwable? = null) :
    BaseException(Exception(t), "Error while parsing line: '$line'.")

class DBInsertException(t: Throwable? = null) :
    BaseException(
        Exception(t),
        "Trying to insert or update session data after the DB has been cleaned."
    )

class MissingDeviceAfterConnectionError : BaseException()

// SD card sync

class SDCardMissingSDCardUploadFixedMeasurementsServiceError : BaseException()

class SDCardMissingSessionsSyncServiceError : BaseException()

class SDCardSessionsInitialSyncError(cause: Exception? = null) :
    BaseException(cause, "There was a problem while refreshing sessions list.")

class SDCardMeasurementsParsingError(cause: Exception) :
    BaseException(cause, "There was a problem while parsing measurements from SD card.")

class SDCardDownloadedFileCorrupted :
    BaseException(null, "Download from SD card was corrupted.")

class SDCardSessionsFinalSyncError(cause: Exception? = null) :
    BaseException(cause, "There was a problem while sending mobile sessions to the backend.")

class NotesNoLocationError :
    BaseException(
        null,
        "Note could not be added because location of last measurement could not be retrieved."
    )

class CSVGenerationError :
    BaseException(null, "There was a problem while generating a CSV file.")

class SensorDisconnectedError(additionalMessage: String?) :
    BaseException(null, "RECONNECTION LOGS: ${additionalMessage}")

class ParseDateError(cause: Exception? = null) :
    BaseException(cause, "There was a problem when parsing a date")

class SDCardSyncError(additionalMessage: String?) :
    BaseException(null, "SD CARD SYNC LOGS: ${additionalMessage}")

class AirbeamServiceError(additionalMessage: String?) :
    BaseException(null, "AirBeam service error: ${additionalMessage}")

object ThresholdAlert {
    class SaveChangesError(cause: Throwable?) :
        BaseException(null, "Threshold Alert saving error: ${cause?.message}")
}

object Account {
    class DeleteError(cause: Throwable?) : BaseException(Exception(cause))
}
