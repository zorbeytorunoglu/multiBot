package com.zorbeytorunoglu.multiBot.commands.configuration

import com.zorbeytorunoglu.multiBot.utils.FileUtils
import java.io.File

class CommandsConfigurationHandler {

    val commands: Commands

    init {
        commands = loadCommands()
    }

    private fun loadCommands(): Commands {

        val file = File("commands.json")

        return FileUtils.loadFromJson(file, Commands::class.java)

    }

}