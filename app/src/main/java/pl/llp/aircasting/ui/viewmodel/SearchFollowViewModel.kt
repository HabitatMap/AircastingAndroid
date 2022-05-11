package pl.llp.aircasting.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.data.api.repositories.ActiveFixedSessionsInRegionRepository
import pl.llp.aircasting.data.api.util.SensorInformation
import pl.llp.aircasting.data.model.GeoSquare
import pl.llp.aircasting.util.Resource
import pl.llp.aircasting.util.isConnected
import javax.inject.Inject

class SearchFollowViewModel @Inject constructor(
    private val activeFixedRepo: ActiveFixedSessionsInRegionRepository,
    private val mApp: AircastingApplication
) : ViewModel() {
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