package pl.llp.aircasting.ui.viewmodel

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.repositories.ActiveFixedSessionsInRegionRepository
import pl.llp.aircasting.data.api.util.SensorInformation
import pl.llp.aircasting.data.model.GeoSquare
import pl.llp.aircasting.util.Resource
import pl.llp.aircasting.util.isConnected
import javax.inject.Inject

class SearchFollowViewModel @Inject constructor(
    private val activeFixedRepo: ActiveFixedSessionsInRegionRepository,
    private val mApp: AircastingApplication
) : AndroidViewModel(mApp) {
    fun getSessionsInRegion(square: GeoSquare, sensorInfo: SensorInformation) =
        liveData(Dispatchers.IO) {
            emit(Resource.loading(null))

            try {

                if (mApp.isConnected)
                    activeFixedRepo.getSessionsFromRegion(square, sensorInfo).let {
                        if (it.isSuccessful) emit(Resource.success(it.body()?.sessions))
                        else emit(Resource.error(null, it.errorBody().toString()))
                    }
                else emit(
                    Resource.error(
                        null,
                        mApp.getString(R.string.fixed_session_no_internet_connection)
                    )
                )

            } catch (e: Exception) {
                emit(Resource.error(null, message = e.message.toString()))
            }
        }
}