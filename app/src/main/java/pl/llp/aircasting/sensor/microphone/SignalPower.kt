package pl.llp.aircasting.sensor.microphone


/**
 * dsp: various digital signal processing algorithms
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

/**
 * A power metering algorithm.
 */
class SignalPower {
    internal var MIN_DB = -100.0
    internal var MAX_DB = 0.0

    // ******************************************************************** //
    // Constants.
    // ******************************************************************** //

    // Maximum signal amplitude for 16-bit data.
    private val MAX_16_BIT = 32768f

    // This fudge factor is added to the output to make a realistically
    // fully-saturated signal come to 0dB.  Without it, the signal would
    // have to be solid samples of -32768 to read zero, which is not
    // realistic.  This really is a fudge, because the best value depends
    // on the input frequency and sampling rate.  We optimise here for
    // a 1kHz signal at 16,000 samples/sec.
    private val FUDGE = 0.6f

    fun calculatePowerDb(data: ShortArray): Double? {
        val power = calculatePowerDb(data, 0, data.size)
        return if (power < MAX_DB && power > MIN_DB) {
            power
        } else {
            // Faulty data
            null
        }
    }

    /**
     * Calculate the power of the given input signal.
     *
     * @param sdata   Buffer containing the input samples to process.
     * @param off     Offset in sdata of the data of interest.
     * @param samples Number of data samples to process.
     * @return The calculated power in dB relative to the maximum
     * input level; hence 0dB represents maximum power,
     * and minimum power is about -95dB.  Particular
     * cases of interest:
     *
     *  * A non-clipping full-range sine wave input is
     * about -2.41dB.
     *  * Saturated input (heavily clipped) approaches
     * 0dB.
     *  * A low-frequency fully saturated input can
     * get above 0dB, but this would be pretty
     * artificial.
     *  * A really tiny signal, which only occasionally
     * deviates from zero, can get below -100dB.
     *  * A completely zero input will produce an
     * output of -Infinity.
     *
     * **You must be prepared to handle this infinite
     * result and results greater than zero,** although
     * clipping them off would be quite acceptable in
     * most cases.
     */
    private fun calculatePowerDb(sdata: ShortArray, off: Int, samples: Int): Double {
        // Calculate the sum of the values, and the sum of the squared values.
        // We need longs to avoid running out of bits.
        var sum = 0.0
        var sqsum = 0.0
        for (i in 0 until samples) {
            val v = sdata[off + i].toLong()
            sum += v.toDouble()
            sqsum += (v * v).toDouble()
        }

        // sqsum is the sum of all (signal+bias)², so
        //     sqsum = sum(signal²) + samples * bias²
        // hence
        //     sum(signal²) = sqsum - samples * bias²
        // Bias is simply the average value, i.e.
        //     bias = sum / samples
        // Since power = sum(signal²) / samples, we have
        //     power = (sqsum - samples * sum² / samples²) / samples
        // so
        //     power = (sqsum - sum² / samples) / samples
        var power = (sqsum - sum * sum / samples) / samples

        // Scale to the range 0 - 1.
        power /= (MAX_16_BIT * MAX_16_BIT).toDouble()

        // Convert to dB, with 0 being max power.  Add a fudge factor to make
        // a "real" fully saturated input come to 0 dB.
        return Math.log10(power) * 10f + FUDGE
    }
}

