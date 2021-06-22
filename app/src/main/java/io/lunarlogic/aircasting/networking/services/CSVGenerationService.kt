package io.lunarlogic.aircasting.networking.services

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import io.lunarlogic.aircasting.lib.CSVHelper
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.networking.responses.SyncResponse
import retrofit2.Call
import java.io.IOException

class CSVGenerationService(
    private val session: Session,
    private val context: Context,
    private val csvHelper: CSVHelper
    ) {
        private val thread = CSVThread()

        fun start() {
            thread.start()
        }

        fun stop() {
            thread.cancel()
        }

        fun pause() {
            thread.paused = true
        }

        fun resume() {
            thread.paused = false
        }

        inner class CSVThread(): Thread() {
            var paused = false
            private var call: Call<SyncResponse>? = null


            override fun run() {
                try {
                    val uri = prepareAndShare()
                    if (uri == null) {
                        println("Error csv")
                    } else {
                        val sendIntent = Intent()
                        sendIntent.action = Intent.ACTION_SEND
                        sendIntent.putExtra(Intent.EXTRA_STREAM, uri)
                        sendIntent.type = "application/zip"
                        context.startActivity(Intent.createChooser(sendIntent, "SHARE"))
                    }
                } catch (e: InterruptedException) {
                    return
                }
            }

            fun cancel() {
                interrupt()
                call?.cancel()
            }

            private fun prepareAndShare(): Uri? {
                return try {
                    csvHelper.prepareCSV(context, session)
                } catch (e: IOException) {
//                    Log.e("Error while creating session CSV", e)
                    null
                }


            }
        }
}
