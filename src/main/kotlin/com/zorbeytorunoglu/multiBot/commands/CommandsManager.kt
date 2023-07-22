package com.zorbeytorunoglu.multiBot.commands

import com.zorbeytorunoglu.multiBot.Bot
import com.zorbeytorunoglu.multiBot.commands.admin.PermissionCommand
import com.zorbeytorunoglu.multiBot.commands.audio.RecordCommand
import com.zorbeytorunoglu.multiBot.commands.misc.PingCommand
import com.zorbeytorunoglu.multiBot.commands.misc.RemindCommand
import com.zorbeytorunoglu.multiBot.commands.misc.SayCommand
import com.zorbeytorunoglu.multiBot.commands.misc.TranscriptCommand
import com.zorbeytorunoglu.multiBot.commands.moderation.KickCommand
import com.zorbeytorunoglu.multiBot.commands.task.TaskCommand
import com.zorbeytorunoglu.multiBot.commands.ticket.TicketCommand
import com.zorbeytorunoglu.multiBot.commands.ticket.TicketPanelCommand
import kotlinx.coroutines.*
import net.dv8tion.jda.api.interactions.commands.build.Commands

class CommandsManager(private val bot: Bot) {

    val commands: List<Command> = listOf(
        PingCommand(bot),
        TicketPanelCommand(bot),
        KickCommand(bot),
        RemindCommand(bot),
        RecordCommand(bot),
        PermissionCommand(bot),
        SayCommand(bot),
        TicketCommand(bot),
        TranscriptCommand(bot),
        TaskCommand(bot)
        )

    init {

        println("Wait for all the commands to be registered! It may take a few minutes...")

        CoroutineScope(Dispatchers.IO).launch {
            registerCommands().await()
            println("All the commands are registered!")
        }

    }

    private suspend fun registerCommands(): CompletableDeferred<Unit> {

        val deferred = CompletableDeferred<Unit>()

        commands.forEach { command ->

            delay(2500)

            val data = Commands.slash(command.name, command.description)
            if (command.optionData().isNotEmpty())
                data.addOptions(command.optionData())
            if (command.subcommandData().isNotEmpty())
                data.addSubcommands(command.subcommandData())

            bot.jda.upsertCommand(data).queue {
                println("Command /${command.name} is registered.")
                if (commands.indexOf(command) == commands.lastIndex) {
                    deferred.complete(Unit)
                }
            }

        }

        return deferred

    }

}