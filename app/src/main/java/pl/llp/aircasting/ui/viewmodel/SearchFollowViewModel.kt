package pl.llp.aircasting.ui.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.*
import pl.llp.aircasting.data.api.repository.ActiveFixedSessionsInRegionRepository
import pl.llp.aircasting.data.api.response.StreamOfGivenSessionResponse
import pl.llp.aircasting.data.api.response.search.SessionInRegionResponse
import pl.llp.aircasting.data.api.response.search.session.details.SessionWithStreamsAndMeasurementsResponse
import pl.llp.aircasting.data.api.util.SensorInformation
import pl.llp.aircasting.data.local.repository.*
import pl.llp.aircasting.data.model.*
import pl.llp.aircasting.di.modules.IoDispatcher
import pl.llp.aircasting.di.modules.MainDispatcher
import pl.llp.aircasting.util.Resource
import javax.inject.Inject

class SearchFollowViewModel @Inject constructor(
    private val activeFixedRepo: ActiveFixedSessionsInRegionRepository,
    private val measurementsRepository: MeasurementsRepository,
    private val activeSessionMeasurementsRepository: ActiveSessionMeasurementsRepository,
    private val measurementStreamsRepository: MeasurementStreamsRepository,
    private val sessionsRepository: SessionsRepository,
    private val thresholdsRepository: ThresholdsRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    private val mutableSelectedSession = MutableLiveData<SessionInRegionResponse>()
    private val mutableLat = MutableLiveData<Double>()
    private val mutableLng = MutableLiveData<Double>()
    private val mutableThresholdColor = MutableLiveData<Int>()
    private lateinit var selectedFullSession: Deferred<Session?>

    val selectedSession: LiveData<SessionInRegionResponse> get() = mutableSelectedSession
    val myLat: LiveData<Double> get() = mutableLat
    val myLng: LiveData<Double> get() = mutableLng
    val thresholdColor: LiveData<Int> get() = mutableThresholdColor
    lateinit var isSelectedSessionFollowed: Deferred<Boolean>

    fun selectSession(session: SessionInRegionResponse) {
        mutableSelectedSession.value = session

        isSelectedSessionFollowed = checkIfSessionIsFollowedAsync()

        val selectedSessionWithStreamsResponse = downloadFullSessionAsync(session)
        selectedFullSession = initializeModelFromResponseAsync(selectedSessionWithStreamsResponse)
    }

    private fun checkIfSessionIsFollowedAsync(): Deferred<Boolean> {
        return viewModelScope.async(ioDispatcher) {
            selectedSession.value?.uuid?.let { sessionsRepository.getSessionByUUID(it) } != null
        }
    }

    private fun downloadFullSessionAsync(session: SessionInRegionResponse) =
        viewModelScope.async(ioDispatcher) {
            activeFixedRepo.getSessionWithStreamsAndMeasurements(
                session.id
            )
        }

    private fun initializeModelFromResponseAsync(
        selectedSessionWithStreamsResponse
        : Deferred<Resource<SessionWithStreamsAndMeasurementsResponse>>
    ): Deferred<Session?> =
        viewModelScope.async {
            val response = selectedSessionWithStreamsResponse.await().data
            val streams = getStreamsWithMeasurementsFromResponse(response)
            val sessionInRegionResponse = selectedSession.value

            if (sessionInRegionResponse != null && streams != null) {
                return@async Session(sessionInRegionResponse, streams)
            }
            return@async null
        }

    private fun getStreamsWithMeasurementsFromResponse(response: SessionWithStreamsAndMeasurementsResponse?) =
        response?.sensors?.map { stream ->
            val measurements = stream.measurements?.map { measurement -> Measurement(measurement) }
            MeasurementStream(stream, measurements)
        }

    fun getStreams() = liveData(ioDispatcher) {
        val response = selectedFullSession.await()
        emit(response)
    }

    fun selectColor(color: Int) {
        mutableThresholdColor.value = color
    }

    fun getLat(lat: Double) {
        mutableLat.value = lat
    }

    fun getLng(lng: Double) {
        mutableLng.value = lng
    }

    fun saveSession() {
        viewModelScope.launch(ioDispatcher) {
            val session = selectedFullSession.await()
            if (session != null) {
                val thresholds = thresholdsRepository.findOrCreateSensorThresholds(session)

                setSessionThresholdsAccordingToUserSettings(session, thresholds)

                val sessionId = saveSessionToDB(session)

                saveStreamsAndTheirMeasurements(
                    sessionId,
                    session.streams
                )
            }
        }
    }

    private fun setSessionThresholdsAccordingToUserSettings(
        session: Session,
        thresholds: List<SensorThreshold>
    ) {
        session.streams.forEach { stream ->
            val threshold = thresholds.find { it.sensorName == stream.sensorName }
            if (threshold != null)
                updateStreamThresholds(stream, threshold)
        }
    }

    private fun updateStreamThresholds(
        stream: MeasurementStream,
        threshold: SensorThreshold
    ) {
        stream.apply {
            thresholdLow = threshold.thresholdLow
            thresholdMedium = threshold.thresholdMedium
            thresholdHigh = threshold.thresholdHigh
            thresholdVeryHigh = threshold.thresholdVeryHigh
        }
    }

    private suspend fun saveSessionToDB(
        session: Session
    ): Long {
        val sessionId = viewModelScope.async(ioDispatcher) {
            sessionsRepository.insert(session)
        }
        return sessionId.await()
    }

    private suspend fun saveStreamsAndTheirMeasurements(
        sessionId: Long,
        streams: List<MeasurementStream>
    ) {
        streams.forEach { stream ->
            val streamId = insertStreamToDB(sessionId, stream)
            insertMeasurementsToDB(streamId, sessionId, stream)
        }
    }

    private suspend fun insertStreamToDB(
        sessionId: Long,
        stream: MeasurementStream
    ) = withContext(viewModelScope.coroutineContext + ioDispatcher) {
        measurementStreamsRepository.insert(
            sessionId,
            stream
        )
    }

    private fun insertMeasurementsToDB(
        streamId: Long,
        sessionId: Long,
        stream: MeasurementStream
    ) {
        measurementsRepository.insertAll(streamId, sessionId, stream.measurements)
        activeSessionMeasurementsRepository.insertAll(streamId, sessionId, stream.measurements)
    }

    fun deleteSession(session: SessionInRegionResponse) {
        viewModelScope.launch(ioDispatcher) {
            sessionsRepository.delete(session.uuid)
        }
    }

    fun getSessionsInRegion(square: GeoSquare, sensorInfo: SensorInformation) =
        liveData(ioDispatcher) {
            emit(Resource.loading(null))

            try {
                val mSessions =
                    activeFixedRepo.getSessionsFromRegion(square, sensorInfo)
                emit(mSessions)
            } catch (e: Exception) {
                emit(Resource.error(null, message = e.message.toString()))
            }
        }

    fun getLastStreamFromSelectedSession(
        sessionId: Long,
        sensorName: String
    ): LiveData<Resource<StreamOfGivenSessionResponse>> =
        liveData(ioDispatcher) {
            emit(Resource.loading(null))

            val stream = activeFixedRepo.getStreamOfGivenSession(
                sessionId,
                sensorName
            )
            emit(stream)
        }
}