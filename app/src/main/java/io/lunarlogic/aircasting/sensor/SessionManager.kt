package io.lunarlogic.aircasting.sensor

import android.content.Context
import android.widget.Toast
import io.lunarlogic.aircasting.events.NewMeasurementEvent
import io.lunarlogic.aircasting.events.StartRecordingEvent
import io.lunarlogic.aircasting.events.StopRecordingEvent
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.exceptions.InternalAPIError
import io.lunarlogic.aircasting.exceptions.UnexpectedAPIError
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.ApiServiceFactory
import io.lunarlogic.aircasting.networking.CreateSessionBody
import io.lunarlogic.aircasting.networking.GzippedSession
import io.lunarlogic.aircasting.networking.SessionParams
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SessionManager(private val mContext: Context, private val settings: Settings) {
    private val mErrorHandler = ErrorHandler(mContext)
    private var mCurrentSession: Session? = null
    private val apiService = ApiServiceFactory.get(settings.getAuthToken()!!)

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
        mCurrentSession?.addMeasurement(event)
    }

    private fun startRecording(session: Session) {
        mCurrentSession = session
    }

    private fun stopRecording() {
        val session = mCurrentSession!!
        session.stop()

        val sessionParams = SessionParams(session)
        val sessionBody = CreateSessionBody(GzippedSession.get(sessionParams))
        val call = apiService.createSession(sessionBody)
        call.enqueue(object : Callback<Session> {
            override fun onResponse(call: Call<Session>, response: Response<Session>) {
                println(response.message())
                if (response.isSuccessful) {
                    val message = "Session created successfully! Check http://aircasting.habitatmap.org/mobile_map"
                    val toast = Toast.makeText(mContext, message, Toast.LENGTH_LONG)
                    toast.show()
                } else {
                    mErrorHandler.handleAndDisplay(UnexpectedAPIError())
                }
            }

            override fun onFailure(call: Call<Session>, t: Throwable) {
                mErrorHandler.handleAndDisplay(UnexpectedAPIError(t))
            }
        })
    }
}