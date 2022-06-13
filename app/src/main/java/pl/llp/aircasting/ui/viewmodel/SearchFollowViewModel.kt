package pl.llp.aircasting.ui.viewmodel

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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
import pl.llp.aircasting.util.Resource
import javax.inject.Inject

class SearchFollowViewModel @Inject constructor(
    private val activeFixedRepo: ActiveFixedSessionsInRegionRepository,
    private val measurementsRepository: MeasurementsRepository,
    private val activeSessionMeasurementsRepository: ActiveSessionMeasurementsRepository,
    private val measurementStreamsRepository: MeasurementStreamsRepository,
    private val sessionsRepository: SessionsRepository
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
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ) {
        viewModelScope.launch(dispatcher) {
            val sessionId =
                saveSession(dispatcher, session)

            val streamId =
                saveMeasurementStream(
                    dispatcher,
                    sessionId,
                    MeasurementStream(session.streams.sensor)
                )
            measurements = getMeasurementsFromSelectedSession(dispatcher)
            saveMeasurements(dispatcher, streamId, sessionId, measurements)
            saveMeasurementsToActiveTable(dispatcher, streamId, sessionId, measurements)
        }
    }

    private fun saveMeasurementsToActiveTable(
        dispatcher: CoroutineDispatcher,
        streamId: Long,
        sessionId: Long,
        measurements: List<Measurement>
    ) {
        viewModelScope.launch(dispatcher) {
            activeSessionMeasurementsRepository.insertAll(streamId, sessionId, measurements)
        }
    }

    private fun saveMeasurements(
        dispatcher: CoroutineDispatcher,
        streamId: Long,
        sessionId: Long,
        measurements: List<Measurement>
    ) {
        viewModelScope.launch(dispatcher) {
            measurementsRepository.insertAll(streamId, sessionId, measurements)
        }
    }

    private suspend fun saveSession(
        dispatcher: CoroutineDispatcher,
        session: SessionInRegionResponse
    ): Long {
        val sessionId = viewModelScope.async(dispatcher) {
            sessionsRepository.insert(Session(session))
        }
        return sessionId.await()
    }

    private suspend fun saveMeasurementStream(
        dispatcher: CoroutineDispatcher,
        sessionId: Long,
        measurementStream: MeasurementStream
    ): Long {
        val measurementStreamId = viewModelScope.async(dispatcher) {
            measurementStreamsRepository.insert(
                sessionId,
                measurementStream
            )
        }
        return measurementStreamId.await()
    }

    fun onUnfollowSessionClicked(
        session: SessionInRegionResponse,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ) {
        viewModelScope.launch(dispatcher) {
            sessionsRepository.delete(listOf(session.uuid))
        }
    }

    fun getSessionsInRegion(square: GeoSquare, sensorInfo: SensorInformation) =
        liveData(Dispatchers.IO) {
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
        liveData(Dispatchers.IO) {
            emit(Resource.loading(null))

            val stream = activeFixedRepo.getStreamOfGivenSession(
                sessionId,
                sensorName
            )
            emit(stream)
        }

    private suspend fun getMeasurementsFromSelectedSession(dispatcher: CoroutineDispatcher): List<Measurement> {
        val sessionId = selectedSession.value?.id?.toLong()
        val sensorName = selectedSession.value?.streams?.sensor?.sensorName
        val measurementLimit = Constants.MEASUREMENTS_IN_HOUR * 24

        if (sensorName != null && sessionId != null) {
            val response = viewModelScope.async(dispatcher) {
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