package com.zorbeytorunoglu.multiBot.audio

import net.dv8tion.jda.api.audio.AudioReceiveHandler
import net.dv8tion.jda.api.audio.AudioSendHandler
import net.dv8tion.jda.api.audio.CombinedAudio
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentLinkedQueue

class RecordingAudioHandler: AudioReceiveHandler, AudioSendHandler {

    private val queue = ConcurrentLinkedQueue<ByteArray>()
    private val mp3Encoder = Mp3Encoder()
    private val outputStream = ByteArrayOutputStream()
    private val outputStreamSize = 491520

    override fun handleCombinedAudio(combinedAudio: CombinedAudio) {

        val volume = 1.0
        val data = combinedAudio.getAudioData(volume)

        try {
            queue.add(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun canReceiveCombined(): Boolean {
        return queue.size < 200
    }

    override fun canProvide(): Boolean {
        return queue.isNotEmpty()
    }

    override fun provide20MsAudio(): ByteBuffer? {
        val dataPoll = queue.poll()

        try {
            outputStream.write(dataPoll)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (outputStream.size() === outputStreamSize) {
            val data = outputStream.toByteArray()
            mp3Encoder.encodePcmToMp3(data)
            outputStream.reset()
        }

        return null

    }

    override fun isOpus(): Boolean {
        return false
    }


}