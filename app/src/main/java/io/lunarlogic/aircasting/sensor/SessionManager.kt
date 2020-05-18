package io.lunarlogic.aircasting.sensor

import android.content.Context
import android.widget.Toast
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.SessionDBObject
import io.lunarlogic.aircasting.events.NewMeasurementEvent
import io.lunarlogic.aircasting.events.StartRecordingEvent
import io.lunarlogic.aircasting.events.StopRecordingEvent
import io.lunarlogic.aircasting.exceptions.ErrorHandler
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
import java.util.*
import kotlin.collections.HashMap

class SessionManager(private val mContext: Context, private val settings: Settings) {
    private val mErrorHandler = ErrorHandler(mContext)
    private var mActiveSessions: HashMap<String, Session> = hashMapOf()
    private val apiService = ApiServiceFactory.get(settings.getAuthToken()!!)
    private val mDatabase = DatabaseProvider.get(mContext)
    private var mSession: Session? = null

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
        stopRecording(event.sessionUUID)
    }

    @Subscribe
    fun onMessageEvent(event: NewMeasurementEvent) {
        val session = mActiveSessions.get(event.deviceId)
        session?.addMeasurement(event)
    }

    private fun startRecording(session: Session) {
        session.startRecording()
        mActiveSessions.put(session.deviceId, session)
        this.mSession = session // TODO: remove after interface change

        DatabaseProvider.runQuery {
            val sessionDBObject = SessionDBObject(session)
            mDatabase.sessions().insert(sessionDBObject)
        }
    }

    private fun stopRecording(sessionUUID: UUID) {
        // val session = getActiveSessionByUUID(sessionUUID)
        val session = mSession // TODO: change to the upper line after interface change
        session?.let {
            it.stopRecording()
            saveToBackend(it)
        }
    }

    private fun getActiveSessionByUUID(uuid: UUID): Session? {
        return mActiveSessions.filterValues { session -> session.uuid == uuid }.values.toList()[0]
    }

    private fun saveToBackend(session: Session) {
        val sessionParams = SessionParams(session)
        val sessionBody = CreateSessionBody(GzippedSession.get(sessionParams))
        val call = apiService.createSession(sessionBody)
        call.enqueue(object : Callback<Session> {
            override fun onResponse(call: Call<Session>, response: Response<Session>) {
                println(response.message())
                if (response.isSuccessful) {
                    val message = "SessionDBObject created successfully! Check http://aircasting.habitatmap.org/mobile_map"
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