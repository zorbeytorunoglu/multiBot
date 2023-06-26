package com.zorbeytorunoglu.multiBot.listeners

import com.zorbeytorunoglu.multiBot.Bot
import com.zorbeytorunoglu.multiBot.commands.CommandsManager
import com.zorbeytorunoglu.multiBot.commands.Listener
import com.zorbeytorunoglu.multiBot.commands.configuration.CommandsConfigurationHandler
import com.zorbeytorunoglu.multiBot.messages.MessagesHandler
import com.zorbeytorunoglu.multiBot.permissions.PermissionManager
import com.zorbeytorunoglu.multiBot.settings.SettingsHandler
import com.zorbeytorunoglu.multiBot.ticket.TicketManager
import com.zorbeytorunoglu.multiBot.ticket.configuration.TicketConfigurationHandler
import com.zorbeytorunoglu.multiBot.ticket.listeners.TicketButtonListener
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ReadyListener(private val bot: Bot): ListenerAdapter() {

    override fun onReady(event: ReadyEvent) {

        bot.jda = event.jda

        bot.messagesHandler = MessagesHandler()
        bot.commandsConfigurationHandler = CommandsConfigurationHandler()
        bot.permissionManager = PermissionManager()
        bot.commandsManager = CommandsManager(bot)
        bot.ticketHandler = TicketManager(TicketConfigurationHandler())

        bot.jda.addEventListener(
            Listener(bot),
            TicketButtonListener(bot)
        )

    }

}