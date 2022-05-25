package pl.llp.aircasting.ui.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.data.api.repository.ActiveFixedSessionsInRegionRepository
import pl.llp.aircasting.data.api.response.StreamOfGivenSessionResponse
import pl.llp.aircasting.data.api.response.search.Sensor
import pl.llp.aircasting.data.api.response.search.Session
import pl.llp.aircasting.data.api.util.SensorInformation
import pl.llp.aircasting.data.model.GeoSquare
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter
import pl.llp.aircasting.util.Resource
import pl.llp.aircasting.util.isConnected
import javax.inject.Inject

class SearchFollowViewModel @Inject constructor(
    private val activeFixedRepo: ActiveFixedSessionsInRegionRepository,
    private val mApp: AircastingApplication
) : ViewModel() {
    private val mutableSelectedSession = MutableLiveData<Session>()
    private val mutableLat = MutableLiveData<Double>()
    private val mutableLng = MutableLiveData<Double>()
    private val mutableThresholdColor = MutableLiveData<Int>()
    private var mSessionPresenter: SessionPresenter? = null

    val selectedSession: LiveData<Session> get() = mutableSelectedSession
    val myLat: LiveData<Double> get() = mutableLat
    val myLng: LiveData<Double> get() = mutableLng
    val thresholdColor: LiveData<Int> get() = mutableThresholdColor

    fun selectSession(session: Session) {
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

    fun onFollowSession() = viewModelScope.launch(Dispatchers.IO) {
        //
    }

    fun getSessionsInRegion(square: GeoSquare, sensorInfo: SensorInformation) =
        liveData(Dispatchers.IO) {
            emit(Resource.loading(null))

            try {
                if (mApp.isConnected) {
                    val mSessions =
                        activeFixedRepo.getSessionsFromRegion(square, sensorInfo)
                    emit(mSessions)
                } else {
                    //TODO - load from DB or show error message
                }
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
}