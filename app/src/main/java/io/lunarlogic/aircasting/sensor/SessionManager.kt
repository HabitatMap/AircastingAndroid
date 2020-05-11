package io.lunarlogic.aircasting.sensor

import io.lunarlogic.aircasting.events.NewMeasurementEvent
import io.lunarlogic.aircasting.events.StartRecordingEvent
import io.lunarlogic.aircasting.events.StopRecordingEvent
import io.lunarlogic.aircasting.networking.ApiServiceFactory
import io.lunarlogic.aircasting.networking.CreateSessionBody
import io.lunarlogic.aircasting.networking.GzippedSession
import io.lunarlogic.aircasting.networking.SessionParams
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SessionManager {
    private var mCurrentSession: Session? = null
    private val apiService = ApiServiceFactory.get("Eqha7roSkYfgKvyLYHHx")

    fun registerToEventBus() {
        EventBus.getDefault().register(this);
    }

    fun unregisterFromEventBus() {
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    fun onMessageEvent(event: StartRecordingEvent) {
        startRecording(event.session)
    }

    @Subscribe
    fun onMessageEvent(event: StopRecordingEvent) {
        stopRecording()
    }

    @Subscribe
    fun onMessageEvent(event: NewMeasurementEvent) {
        // TODO: handle multiple sessions
        mCurrentSession?.addMeasurement(event.measurement)
    }

    private fun startRecording(session: Session) {
        mCurrentSession = session
    }

    private fun stopRecording() {
        val session = mCurrentSession!!
        session.stop()
        try {
            val sessionParams = SessionParams(session)
            val sessionBody = CreateSessionBody(GzippedSession.get(sessionParams))
            val call = apiService.createSession(sessionBody)
            call.enqueue(object : Callback<Session> {
                override fun onResponse(call: Call<Session>, response: Response<Session>) {
                    println("ANIA: SUCCESS!")
                    println(response.message())
                }

                override fun onFailure(call: Call<Session>, t: Throwable) {
                    println("ANIA: ERROR! :(")
                }

            })
        } catch(e: Exception) {
            // TODO handle?
            e.printStackTrace()
        }
    }
}