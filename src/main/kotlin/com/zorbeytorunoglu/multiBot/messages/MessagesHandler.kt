package com.zorbeytorunoglu.multiBot.messages

import com.zorbeytorunoglu.multiBot.utils.FileUtils
import java.io.File

class MessagesHandler {

    val messages: Messages

    init {

        messages = loadMessages()

    }

    private fun loadMessages(): Messages {

        val file = File("messages.json")

        return FileUtils.loadFromJson(file, Messages::class.java)

    }

}