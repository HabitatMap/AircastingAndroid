package pl.llp.aircasting.utilities

import com.google.gson.Gson
import pl.llp.aircasting.data.api.response.StreamOfGivenSessionResponse
import pl.llp.aircasting.data.api.response.search.SessionsInRegionsRes

class TestHelper {
    companion object {
        fun mockGetSessionsInRegionResponseWithJson(json: String): SessionsInRegionsRes {
            return Gson().fromJson(json, SessionsInRegionsRes::class.java)
        }

        fun mockGetStreamOfGivenSessionResponseWithJson(json: String): StreamOfGivenSessionResponse {
            return Gson().fromJson(json, StreamOfGivenSessionResponse::class.java)
        }
    }
}