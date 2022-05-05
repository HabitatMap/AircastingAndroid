package pl.llp.aircasting.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import pl.llp.aircasting.data.api.repositories.SearchFollowRepository
import pl.llp.aircasting.util.Resource
import javax.inject.Inject

class SearchFollowViewModel @Inject constructor(
    private val searchFollowRepo: SearchFollowRepository
) : ViewModel() {
    fun getSessionsInRegion(query: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))

        try {
                val getSessions = searchFollowRepo.getSessionsInRegion(query)
                emit(Resource.success(data = getSessions))

        } catch (e: Exception) {
            emit(Resource.error(data = null, message = e.message ?: "Error Occurred!"))
        }
    }
}