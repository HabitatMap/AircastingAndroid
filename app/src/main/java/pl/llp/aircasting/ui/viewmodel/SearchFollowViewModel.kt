package pl.llp.aircasting.ui.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import pl.llp.aircasting.data.api.Constants
import pl.llp.aircasting.data.api.repository.ActiveFixedSessionsInRegionRepository
import pl.llp.aircasting.data.api.response.MeasurementOfStreamResponse
import pl.llp.aircasting.data.api.response.StreamOfGivenSessionResponse
import pl.llp.aircasting.data.api.response.search.SessionInRegionResponse
import pl.llp.aircasting.data.api.util.SensorInformation
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.GeoSquare
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.di.modules.IoDispatcher
import pl.llp.aircasting.util.Resource
import javax.inject.Inject

class SearchFollowViewModel @Inject constructor(
    private val activeFixedRepo: ActiveFixedSessionsInRegionRepository,
    private val measurementsRepository: MeasurementsRepository,
    private val activeSessionMeasurementsRepository: ActiveSessionMeasurementsRepository,
    private val measurementStreamsRepository: MeasurementStreamsRepository,
    private val sessionsRepository: SessionsRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    private val mutableSelectedSession = MutableLiveData<SessionInRegionResponse>()
    private val mutableLat = MutableLiveData<Double>()
    private val mutableLng = MutableLiveData<Double>()
    private val mutableThresholdColor = MutableLiveData<Int>()
    private lateinit var measurements: List<Measurement>

    val selectedSession: LiveData<SessionInRegionResponse> get() = mutableSelectedSession
    val myLat: LiveData<Double> get() = mutableLat
    val myLng: LiveData<Double> get() = mutableLng
    val thresholdColor: LiveData<Int> get() = mutableThresholdColor

    fun selectSession(session: SessionInRegionResponse) {
        mutableSelectedSession.value = session
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

    fun onFollowSessionClicked(
        session: SessionInRegionResponse,
    ) {
        viewModelScope.launch(ioDispatcher) {
            val sessionId =
                saveSession(session)

            val streamId =
                saveMeasurementStream(
                    sessionId,
                    MeasurementStream(session.streams.sensor)
                )
            measurements = getMeasurementsFromSelectedSession()
            saveMeasurements(streamId, sessionId, measurements)
            saveMeasurementsToActiveTable(streamId, sessionId, measurements)
        }
    }

    private fun saveMeasurementsToActiveTable(
        streamId: Long,
        sessionId: Long,
        measurements: List<Measurement>
    ) {
        viewModelScope.launch(ioDispatcher) {
            activeSessionMeasurementsRepository.insertAll(streamId, sessionId, measurements)
        }
    }

    private fun saveMeasurements(
        streamId: Long,
        sessionId: Long,
        measurements: List<Measurement>
    ) {
        viewModelScope.launch(ioDispatcher) {
            measurementsRepository.insertAll(streamId, sessionId, measurements)
        }
    }

    private suspend fun saveSession(
        session: SessionInRegionResponse
    ): Long {
        val sessionId = viewModelScope.async(ioDispatcher) {
            sessionsRepository.insert(Session(session))
        }
        return sessionId.await()
    }

    private suspend fun saveMeasurementStream(
        sessionId: Long,
        measurementStream: MeasurementStream
    ): Long {
        val measurementStreamId = viewModelScope.async(ioDispatcher) {
            measurementStreamsRepository.insert(
                sessionId,
                measurementStream
            )
        }
        return measurementStreamId.await()
    }

    fun onUnfollowSessionClicked(
        session: SessionInRegionResponse,
    ) {
        viewModelScope.launch(ioDispatcher) {
            sessionsRepository.delete(listOf(session.uuid))
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

    private suspend fun getMeasurementsFromSelectedSession(): List<Measurement> {
        val sessionId = selectedSession.value?.id?.toLong()
        val sensorName = selectedSession.value?.streams?.sensor?.sensorName
        val measurementLimit = Constants.MEASUREMENTS_IN_HOUR * 24

        if (sensorName != null && sessionId != null) {
            val response = viewModelScope.async(ioDispatcher) {
                activeFixedRepo.getStreamOfGivenSession(
                    sessionId,
                    sensorName,
                    measurementLimit
                )
            }
            val measurementsFromResponse = response.await().data?.measurements
            return convertFromResponseToModel(measurementsFromResponse)
        }
        return listOf()
    }

    private fun convertFromResponseToModel(measurementsFromResponse: List<MeasurementOfStreamResponse>?) =
        measurementsFromResponse?.let { list ->
            list.map {
                Measurement(it)
            }
        } ?: listOf()
}