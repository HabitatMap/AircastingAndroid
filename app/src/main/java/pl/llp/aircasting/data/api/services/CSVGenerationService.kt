package pl.llp.aircasting.data.api.services

import android.content.Context
import android.content.Intent
import android.net.Uri
import pl.llp.aircasting.util.exceptions.CSVGenerationError
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.CSVHelper
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.api.responses.SyncResponse
import retrofit2.Call
import java.io.IOException

class CSVGenerationService(
    private val session: Session,
    private val context: Context,
    private val csvHelper: CSVHelper,
    private val errorHandler: ErrorHandler
    ) {
        private val thread = CSVThread()

        fun start() {
            thread.start()
        }

        fun stop() {
            thread.cancel()
        }

        inner class CSVThread(): Thread() {
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
                    errorHandler.handleAndDisplay(CSVGenerationError())
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
                    errorHandler.handleAndDisplay(CSVGenerationError())
                    null
                }
            }
        }
}
