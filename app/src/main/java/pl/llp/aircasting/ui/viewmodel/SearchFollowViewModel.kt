package pl.llp.aircasting.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.data.api.repositories.ActiveFixedSessionsInRegionRepository
import pl.llp.aircasting.data.api.util.SensorInformation
import pl.llp.aircasting.data.model.GeoSquare
import pl.llp.aircasting.util.Resource
import pl.llp.aircasting.util.isConnected
import pl.llp.aircasting.data.api.responses.search.Session
import javax.inject.Inject

class SearchFollowViewModel @Inject constructor(
    private val activeFixedRepo: ActiveFixedSessionsInRegionRepository,
    private val mApp: AircastingApplication
) : ViewModel() {
    private val mutableSelectedSession = MutableLiveData<Session>()
    val selectedSession: LiveData<Session> get() = mutableSelectedSession

    fun selectSession(session: Session) {
        mutableSelectedSession.value = session
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
}