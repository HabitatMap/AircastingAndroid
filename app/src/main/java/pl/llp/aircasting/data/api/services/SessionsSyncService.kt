package pl.llp.aircasting.data.api.services

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.core.net.toUri
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import pl.llp.aircasting.data.api.params.SyncSessionBody
import pl.llp.aircasting.data.api.params.SyncSessionParams
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.NoteRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.di.modules.IoDispatcher
import pl.llp.aircasting.util.OperationStatus
import pl.llp.aircasting.util.exceptions.DBInsertException
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.SyncError
import pl.llp.aircasting.util.exceptions.UnexpectedAPIError
import pl.llp.aircasting.util.extensions.encodeToBase64
import javax.inject.Inject

@UserSessionScope
class SessionsSyncService @Inject constructor(
    @Authenticated private val apiService: ApiService,
    private val errorHandler: ErrorHandler,
    private val uploadService: MobileSessionUploadService,
    private val downloadService: SessionDownloadService,
    private val removeOldMeasurementsService: RemoveOldMeasurementsService,
    private val sessionRepository: SessionsRepository,
    private val measurementStreamsRepository: MeasurementStreamsRepository,
    private val noteRepository: NoteRepository,
    @IoDispatcher val ioDispatcher: CoroutineDispatcher
) {
    sealed class Result {
        object Success : Result()
        data class Error(val throwable: Throwable?) : Result()
    }

    val syncStatus get(): StateFlow<OperationStatus> = _syncStatus
    private val gson: Gson = Gson()
    private val _syncStatus: MutableStateFlow<OperationStatus> = MutableStateFlow(OperationStatus.Idle)
    suspend fun sync(): kotlin.Result<Result> = withContext(ioDispatcher) {
        val sessions = sessionRepository.allSessionsExceptRecording()
        val syncParams = sessions.map { session -> SyncSessionParams(session) }
        val jsonData = gson.toJson(syncParams)

        val syncResult = runCatching {
            _syncStatus.emit(OperationStatus.InProgress)
            val response = apiService.sync(SyncSessionBody(jsonData))

            if (response.isSuccessful) {
                val body = response.body()
                body?.let {
                    Log.d(TAG, "Updating local sessions from SyncSuspend")
                    delete(body.deleted)
                    removeOldMeasurements()

                    upload(body.upload)
                    download(body.download)
                }
                Result.Success
            } else {
                val exception = Exception("Sync failed with response code ${response.code()}")
                errorHandler.handle(SyncError(exception))

                Result.Error(exception)
            }
        }

        syncResult.onFailure { exception ->
            errorHandler.handle(SyncError(exception))
        }
        _syncStatus.emit(OperationStatus.Idle)

        syncResult
    }

    private suspend fun removeOldMeasurements() {
        removeOldMeasurementsService.removeMeasurementsFromSessions()
    }

    private suspend fun delete(uuids: List<String>) {
        sessionRepository.delete(uuids)
    }

    private suspend fun upload(uuids: List<String>) {
        uuids.forEach { uuid ->
            val session = sessionRepository.loadCompleteSession(uuid)
            val encodedPhotos = getPhotosFromSessionNotes(uuid)

            session ?: return

            if (isUploadable(session) && encodedPhotos != null) {
                uploadSession(session, encodedPhotos)
            }
        }
    }

    private suspend fun getPhotosFromSessionNotes(uuid: String): List<String?>? {
        val sessionID = sessionRepository.getSessionIdByUUID(uuid)
        val mNotes = sessionID?.let { noteRepository.getNotesForSessionWithId(it) }

        val encodedPhotos = mNotes?.filterNotNull()?.map { note ->
            encodeToBase64(note.photo_location?.toUri())
        }
        return encodedPhotos
    }

    private suspend fun uploadSession(session: Session, encodedPhotos: List<String?>) {
        runCatching {
            uploadService.upload(session, encodedPhotos)
        }.onSuccess { response ->
            if (response.isSuccessful) {
                sessionRepository.updateUrlLocation(session, response.body()?.location)
            } else {
                errorHandler.handle(UnexpectedAPIError())
            }
        }.onFailure { exception ->
            errorHandler.handle(UnexpectedAPIError(exception))
        }
    }

    private suspend fun download(uuids: List<String>) {
        uuids.forEach { uuid ->
            downloadService.download(uuid)
                .onSuccess { session ->
                    try {
                        val sessionId = sessionRepository.updateOrCreate(session)
                            ?: return@onSuccess

                        measurementStreamsRepository.insert(sessionId, session.streams)

                        session.notes.forEach { note ->
                            noteRepository.insert(sessionId, note)
                        }
                    } catch (e: SQLiteConstraintException) {
                        errorHandler.handle(DBInsertException(e))
                    }
                }
                .onFailure {
                    errorHandler.handle(UnexpectedAPIError(it))
                }
        }
    }

    private fun isUploadable(session: Session): Boolean {
        return !(session.locationless && session.isMobile() || session.isExternal)
    }
}
