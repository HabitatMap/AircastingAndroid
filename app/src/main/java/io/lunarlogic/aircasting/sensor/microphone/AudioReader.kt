package io.lunarlogic.aircasting.sensor.microphone

/**
 * org.hermit.android.io: Android utilities for accessing peripherals.
 *
 * These classes provide some basic utilities for accessing the builtin
 * interface, at present.
 *
 * <br></br>Copyright 2009 Ian Cameron Smith
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation (see COPYING).
 *
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import java.util.Arrays

/**
 * A class which reads builtin input from the mic in a background thread and
 * passes it to the caller when ready.
 *
 *
 *
 * To use this class, your application must have permission RECORD_AUDIO.
 */
// ******************************************************************** //
// Constructor.
// ******************************************************************** //

/**
 * Create an AudioReader instance.
 */
open class AudioReader {

    // ******************************************************************** //
    // Private Data.
    // ******************************************************************** //

    // Our builtin input device.
    private var audioInput: AudioRecord? = null

    // Our builtin input buffer, and the index of the next item to go in.
    private var inputBuffer: Array<ShortArray>? = null
    private var inputBufferWhich = 0
    private var inputBufferIndex = 0

    // Size of the block to read each time.
    private var inputBlockSize = 0

    // Time in ms to sleep between blocks, to meter the supply rate.
    private var sleepTime: Long = 0

    // Listener for input.
    protected var inputListener: Listener? = null

    // Flag whether the thread should be running.
    @Volatile
    private var running = false

    // The thread, if any, which is currently reading.  Null if not running.
    private var readerThread: Thread? = null

    private val TAG = "/AudioReader" // TODO: handle logging better

    // ******************************************************************** //
    // Public Classes.
    // ******************************************************************** //

    /**
     * Listener for builtin reads.
     */
    abstract class Listener {

        /**
         * An builtin read has completed.
         *
         * @param buffer Buffer containing the data.
         */
        abstract fun onReadComplete(buffer: ShortArray)

        /**
         * An error has occurred.  The reader has been terminated.
         *
         * @param error ERR_XXX code describing the error.
         */
        abstract fun onReadError(error: Int)

        companion object {
            /**
             * Audio read error code: the builtin reader failed to initialise.
             */
            val ERR_INIT_FAILED = 1

            /**
             * Audio read error code: an builtin read failed.
             */
            val ERR_READ_FAILED = 2
        }
    }

    // ******************************************************************** //
    // Run Control.
    // ******************************************************************** //

    /**
     * Start this reader.
     *
     * @param rate     The builtin sampling rate, in samples / sec.
     * @param block    Number of samples of input to read at a time.
     * This is different from the system builtin
     * buffer size.
     * @param listener Listener to be notified on each completed read.
     */
    open fun startReader(rate: Int, block: Int, listener: Listener) {
        Log.i(TAG, "Reader: Start Thread")
        synchronized(this) {
            // Calculate the required I/O buffer size.
            val audioBuf = AudioRecord.getMinBufferSize(
                rate,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            Log.d(TAG, "Will use buffer size: $audioBuf")

            // Set up the builtin input.
            audioInput = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                rate,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                audioBuf
            )
            inputBlockSize = block
            sleepTime = (1000f / (rate.toFloat() / block.toFloat())).toLong()

            // We double inputBlockSize because of Android 5.0 bug,
            // AudioRecord.read(short[] audioData, int offsetInShorts, int sizeInShorts)
            // writes twice as much data to a buffer than it should.
            // We cut it later: Arrays.copyOfRange(buffer, 0, inputBlockSize)
            inputBuffer = Array(2) { ShortArray(inputBlockSize * 2) }
            inputBufferWhich = 0
            inputBufferIndex = 0
            inputListener = listener
            running = true
            readerThread = Thread(Runnable { readerRun() }, "Audio Reader")
            readerThread!!.start()
        }
    }

    /**
     * Stop this reader.
     */
    fun stopReader() {
        Log.i(TAG, "Reader: Signal Stop")
        synchronized(this) {
            running = false
        }
        try {
            if (readerThread != null)
                readerThread!!.join()
        } catch (e: InterruptedException) {
            //Ignore - just stop
        }

        readerThread = null

        // Kill the builtin input.
        synchronized(this) {
            if (audioInput != null) {
                audioInput!!.release()
                audioInput = null
            }
        }

        Log.i(TAG, "Reader: Thread Stopped")
    }

    // ******************************************************************** //
    // Main Loop.
    // ******************************************************************** //

    /**
     * Main loop of the builtin reader.  This runs in our own thread.
     */
    open protected fun readerRun() {
        var buffer: ShortArray
        var index: Int
        var readSize: Int

        var timeout = 5000
        try {
            while (timeout > 0 && audioInput!!.state != AudioRecord.STATE_INITIALIZED) {
                Thread.sleep(50)
                timeout -= 50
            }
        } catch (e: InterruptedException) {
            Log.e(TAG, "Audio reader thread interrupted", e)
        }

        Log.d(TAG, "Audio reader state: " + audioInput!!.state)

        if (audioInput!!.state != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "Audio reader failed to initialize")
            readError(Listener.ERR_INIT_FAILED)
            running = false
            return
        }

        try {
            Log.i(TAG, "Reader: Start Recording")
            audioInput!!.startRecording()
            var stime: Long = 0

            while (running) {
                if (inputBufferIndex == 0) {
                    stime = System.currentTimeMillis()
                }

                if (!running) {
                    break
                }

                readSize = inputBlockSize
                val space = inputBlockSize - inputBufferIndex
                if (readSize > space) {
                    readSize = space
                }
                buffer = inputBuffer!![inputBufferWhich]
                index = inputBufferIndex

                synchronized(buffer) {
                    val nread = audioInput!!.read(buffer, index, readSize)

                    var done = false
                    if (!running) {
                        stopReading()
                        return
                    }

                    if (nread < 0) {
                        Log.e(TAG, "Audio read failed: error $nread")
                        readError(Listener.ERR_READ_FAILED)
                        running = false
                        stopReading()
                        return
                    }
                    val end = inputBufferIndex + nread
                    if (end >= inputBlockSize) {
                        inputBufferWhich = (inputBufferWhich + 1) % 2
                        inputBufferIndex = 0
                        done = true
                    } else {
                        inputBufferIndex = end
                    }

                    if (done) {
                        readDone(Arrays.copyOfRange(buffer, 0, inputBlockSize))

                        // Because our block size is way smaller than the builtin
                        // buffer, we get blocks in bursts, which messes up
                        // the builtin analyzer.  We don't want to be forced to
                        // wait until the analysis is done, because if
                        // the analysis is slow, lag will build up.  Instead
                        // wait, but with a timeout which lets us keep the
                        // input serviced.
                        val etime = System.currentTimeMillis()
                        var sleep = sleepTime - (etime - stime)
                        if (sleep < 5) {
                            sleep = 5
                        }
                        try {
                            Thread.sleep(sleep)
                        } catch (e: InterruptedException) {}
                    }
                }
            }
        } finally {
            stopReading()
        }
    }

    private fun stopReading() {
        Log.i(TAG, "Reader: Stop Recording")
        if (audioInput!!.state == AudioRecord.RECORDSTATE_RECORDING)
            audioInput!!.stop()
    }

    /**
     * Notify the client that a read has completed.
     *
     * @param buffer Buffer containing the data.
     */
    protected fun readDone(buffer: ShortArray) {
        inputListener!!.onReadComplete(buffer)
    }

    /**
     * Notify the client that an error has occurred.  The reader has been
     * terminated.
     *
     * @param code ERR_XXX code describing the error.
     */
    private fun readError(code: Int) {
        inputListener!!.onReadError(code)
    }
}

