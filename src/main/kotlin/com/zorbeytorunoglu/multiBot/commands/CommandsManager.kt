package com.zorbeytorunoglu.multiBot.commands

import com.zorbeytorunoglu.multiBot.Bot
import com.zorbeytorunoglu.multiBot.commands.audio.RecordCommand
import com.zorbeytorunoglu.multiBot.commands.misc.PingCommand
import com.zorbeytorunoglu.multiBot.commands.misc.RemindCommand
import com.zorbeytorunoglu.multiBot.commands.moderation.KickCommand
import com.zorbeytorunoglu.multiBot.commands.ticket.TicketPanelCommand
import net.dv8tion.jda.api.interactions.commands.build.Commands

class CommandsManager(private val bot: Bot) {

    val commands: List<Command> = listOf(
        PingCommand(bot),
        TicketPanelCommand(bot),
        KickCommand(bot),
        RemindCommand(bot),
        RecordCommand(bot)
        )

    init {
        registerCommands()
    }

    private fun registerCommands() {

        commands.forEach { command ->

            val data = Commands.slash(command.name, command.description)
            if (command.optionData().isNotEmpty())
                data.addOptions(command.optionData())
            if (command.subcommandData().isNotEmpty())
                data.addSubcommands(command.subcommandData())
            bot.jda.upsertCommand(data).queue {
                println("${command.name} is registered.")
            }

        }

    }

}