package com.zorbeytorunoglu.multiBot.events.listeners

import com.zorbeytorunoglu.multiBot.Bot
import com.zorbeytorunoglu.multiBot.commands.CommandsManager
import com.zorbeytorunoglu.multiBot.commands.configuration.CommandsConfigurationHandler
import com.zorbeytorunoglu.multiBot.events.AbstractListener
import com.zorbeytorunoglu.multiBot.messages.MessagesHandler
import com.zorbeytorunoglu.multiBot.permissions.PermissionManager
import com.zorbeytorunoglu.multiBot.task.TaskManager
import com.zorbeytorunoglu.multiBot.ticket.TicketManager
import com.zorbeytorunoglu.multiBot.ticket.configuration.TicketConfigurationHandler
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.session.ReadyEvent

class ReadyListener(bot: Bot): AbstractListener(bot) {

    override suspend fun onEvent(event: GenericEvent) {
        if (event is ReadyEvent)
            onReady(event)
    }

    private fun onReady(event: ReadyEvent) {

        bot.jda = event.jda

        bot.messagesHandler = MessagesHandler()
        bot.commandsConfigurationHandler = CommandsConfigurationHandler()
        bot.permissionManager = PermissionManager()
        bot.commandsManager = CommandsManager(bot)

        bot.jda.guilds.forEach {
            it.loadMembers()
        }
        bot.ticketHandler = TicketManager(TicketConfigurationHandler())
        bot.taskManager = TaskManager(bot)

    }

}