package pl.llp.aircasting.ui.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.*
import pl.llp.aircasting.data.api.repository.ActiveFixedSessionsInRegionRepository
import pl.llp.aircasting.data.api.response.search.Sensor
import pl.llp.aircasting.data.api.response.search.SessionInRegionResponse
import pl.llp.aircasting.data.api.response.search.session.details.SessionWithStreamsAndMeasurementsResponse
import pl.llp.aircasting.data.api.util.SensorInformation
import pl.llp.aircasting.data.local.repository.*
import pl.llp.aircasting.data.model.*
import pl.llp.aircasting.di.modules.IoDispatcher
import pl.llp.aircasting.ui.view.screens.dashboard.helpers.SessionFollower
import pl.llp.aircasting.util.Resource
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.extensions.addHours
import pl.llp.aircasting.util.extensions.calendar
import java.util.*
import javax.inject.Inject

class SearchFollowViewModel @Inject constructor(
    private val activeFixedRepo: ActiveFixedSessionsInRegionRepository,
    private val measurementsRepository: MeasurementsRepository,
    private val activeSessionMeasurementsRepository: ActiveSessionMeasurementsRepository,
    private val measurementStreamsRepository: MeasurementStreamsRepository,
    private val sessionsRepository: SessionsRepository,
    private val thresholdsRepository: ThresholdsRepository,
    private val mSettings: Settings,
    private val sessionFollower: SessionFollower,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    private val mutableSelectedSession = MutableLiveData<SessionInRegionResponse>()
    private val mutableLat = MutableLiveData<Double>()
    private val mutableLng = MutableLiveData<Double>()
    private val mutableAddress = MutableLiveData<String>()

    private lateinit var selectedFullSession: Deferred<Session?>
    lateinit var isSelectedSessionFollowed: Deferred<Boolean>

    val selectedSession: LiveData<SessionInRegionResponse> get() = mutableSelectedSession
    val myLat: LiveData<Double> get() = mutableLat
    val myLng: LiveData<Double> get() = mutableLng
    val mSavedAddress: LiveData<String> get() = mutableAddress
    var isOwnSession: Boolean = false

    fun selectSession(session: SessionInRegionResponse) {
        mutableSelectedSession.value = session

        isOwnSession = checkIfUserOwnsSession(session)
        isSelectedSessionFollowed = checkIfSessionIsFollowedAsync()

        val selectedSessionWithStreamsResponse = downloadFullSessionAsync(session)
        selectedFullSession = initializeModelFromResponseAsync(selectedSessionWithStreamsResponse)
    }

    private fun checkIfUserOwnsSession(session: SessionInRegionResponse): Boolean {
        return session.username == mSettings.getProfileName()
    }

    fun userOwnsSession(): Boolean {
        return isOwnSession
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

    private fun initializeModelFromResponseAsync(selectedSessionWithStreamsResponse: Deferred<Resource<SessionWithStreamsAndMeasurementsResponse>>): Deferred<Session?> =
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
            val twentyFourHoursBackFromLastMeasurementTime = lastMeasurementTimeMinus24Hours(stream)
            val twentyFourHoursMeasurements =
                stream.measurements?.filter { it.time >= twentyFourHoursBackFromLastMeasurementTime }
            val measurements =
                twentyFourHoursMeasurements?.map { measurement -> Measurement(measurement) }
            MeasurementStream(stream, measurements)
        }

    private fun lastMeasurementTimeMinus24Hours(stream: Sensor): Long {
        val lastMeasurementTime = stream.measurements?.maxOf { it.time }?.let { Date(it) } ?: Date()
        return calendar().addHours(lastMeasurementTime, -24).time
    }

    fun getStreams() = liveData(ioDispatcher) {
        val response = selectedFullSession.await()
        emit(response)
    }

    fun setLat(lat: Double) {
        mutableLat.value = lat
    }

    fun setLng(lng: Double) {
        mutableLng.value = lng
    }

    fun saveAddressFromSearchFragment(address: String) {
        mutableAddress.value = address
    }

    fun follow() {
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
                sessionFollower.follow(session)
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

    private suspend fun saveSessionToDB(session: Session): Long {
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

    fun getSessionsInRegion(square: GeoSquare, sensorInfo: SensorInformation) =
        liveData(ioDispatcher) {
            emit(Resource.loading(null))

            val mSessions = activeFixedRepo.getSessionsFromRegion(square, sensorInfo)
            emit(mSessions)
        }

    fun unfollow() {
        viewModelScope.launch {
            val session = selectedFullSession.await()
            if (session != null) {
                sessionFollower.unfollow(session)
            }
        }
    }
}