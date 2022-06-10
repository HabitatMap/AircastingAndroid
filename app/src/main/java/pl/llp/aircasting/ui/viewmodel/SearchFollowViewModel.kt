package pl.llp.aircasting.ui.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import pl.llp.aircasting.data.api.repository.ActiveFixedSessionsInRegionRepository
import pl.llp.aircasting.data.api.response.StreamOfGivenSessionResponse
import pl.llp.aircasting.data.api.response.search.SessionInRegionResponse
import pl.llp.aircasting.data.api.util.SensorInformation
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.GeoSquare
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.util.Resource
import javax.inject.Inject

class SearchFollowViewModel @Inject constructor(
    private val activeFixedRepo: ActiveFixedSessionsInRegionRepository,
    private val measurementsRepository: MeasurementsRepository,
    private val measurementStreamsRepository: MeasurementStreamsRepository,
    private val sessionsRepository: SessionsRepository
) : ViewModel() {
    private val mutableSelectedSession = MutableLiveData<SessionInRegionResponse>()
    private val mutableLat = MutableLiveData<Double>()
    private val mutableLng = MutableLiveData<Double>()
    private val mutableThresholdColor = MutableLiveData<Int>()

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

    fun onFollowSessionClicked(session: SessionInRegionResponse, dispatcher: CoroutineDispatcher = Dispatchers.IO) {
        viewModelScope.launch(dispatcher) {
            val sessionId = viewModelScope.async(dispatcher) {
                sessionsRepository.insert(session)
            }

            measurementStreamsRepository.insert(
                sessionId.await(),
                MeasurementStream(session.streams.sensor)
            )
        }
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

    fun insertMeasurements(
        measurementStreamId: Long,
        sessionId: Long,
        measurements: List<Measurement>
    ) {
        measurementsRepository.insertAll(
            measurementStreamId,
            sessionId,
            measurements
        )
    }
}