package com.zorbeytorunoglu.multiBot.commands.configuration

import com.zorbeytorunoglu.multiBot.utils.GsonUtils
import java.io.File

class CommandsConfigurationHandler {

    val commands: Commands

    init {
        commands = loadCommands()
    }

    private fun loadCommands(): Commands {

        val file = File("commands.json")

        return GsonUtils.loadFromJson(file, Commands::class.java)

    }

}