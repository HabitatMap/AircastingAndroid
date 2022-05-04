package pl.llp.aircasting.data.api.services

import pl.llp.aircasting.data.api.responses.SessionsInRegionResponse
import pl.llp.aircasting.data.model.Session
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SessionsInRegionDownloadService(private val apiService: ApiService) {
    fun getSessionsFromRegionToList(square: GeoSquare, sessions: MutableList<Session>) {
        val call = apiService.getSessionsInRegion(
            north = square.north,
            south = square.south,
            east = square.east,
            west = square.west
        )

        call.enqueue(object : Callback<SessionsInRegionResponse> {
            override fun onResponse(
                call: Call<SessionsInRegionResponse>,
                response: Response<SessionsInRegionResponse>
            ) {
                if (response.isSuccessful) {
                    sessions.clear()
                }
            }

            override fun onFailure(call: Call<SessionsInRegionResponse>, t: Throwable) {
                print(t.stackTrace)
            }
        })
    }
}

data class GeoSquare(
    val north: Double,
    val south: Double,
    val east: Double,
    val west: Double
)