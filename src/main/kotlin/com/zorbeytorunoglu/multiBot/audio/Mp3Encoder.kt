package com.zorbeytorunoglu.multiBot.audio

import de.sciss.jump3r.lowlevel.LameEncoder
import de.sciss.jump3r.mp3.Lame
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.sound.sampled.AudioFormat

class Mp3Encoder {

    private val encoder = LameEncoder(
        AudioFormat(48000.0f,16,2,true,true),
        96,2,Lame.STANDARD_FAST,false)
    private val formattedDate: String = SimpleDateFormat("ddMMyyyyHHmm").format(Date())
    private val file = File("$formattedDate.mp3")

    init {
        if (!file.exists()) file.createNewFile()
    }

    fun encodePcmToMp3(pcm: ByteArray): String? {

        try {
            FileOutputStream(file, true).use { mp3 ->
                val buffer = ByteArray(encoder.pcmBufferSize)
                var bytesToTransfer = buffer.size.coerceAtMost(pcm.size)
                var bytesWritten: Int
                var currentPcmPosition = 0
                while (0 < encoder.encodeBuffer(pcm, currentPcmPosition, bytesToTransfer, buffer)
                        .also { bytesWritten = it }
                ) {
                    currentPcmPosition += bytesToTransfer
                    bytesToTransfer = buffer.size.coerceAtMost(pcm.size - currentPcmPosition)
                    mp3.write(buffer, 0, bytesWritten)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
        }

        return file.name
    }


}