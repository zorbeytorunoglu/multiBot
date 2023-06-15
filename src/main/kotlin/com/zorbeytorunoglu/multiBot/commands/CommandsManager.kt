package com.zorbeytorunoglu.multiBot.commands

import com.zorbeytorunoglu.multiBot.Bot
import com.zorbeytorunoglu.multiBot.commands.misc.PingCommand
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands

class CommandsManager(private val bot: Bot) {

    val commands: List<Command> = listOf(PingCommand(bot))

    init {
        registerCommands()
    }

    fun registerCommands() {

        val commandData = mutableListOf<CommandData>()

        commands.forEach {

            val data = Commands.slash(it.name,it.description)
            //TODO: If optionData, subCommand...
            bot.jda.updateCommands().addCommands(data).queue()

        }

    }

}