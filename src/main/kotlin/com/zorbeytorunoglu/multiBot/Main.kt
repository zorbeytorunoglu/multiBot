package com.zorbeytorunoglu.multiBot

import com.zorbeytorunoglu.multiBot.commands.CommandsManager
import com.zorbeytorunoglu.multiBot.commands.Listener
import com.zorbeytorunoglu.multiBot.commands.configuration.CommandsConfigurationHandler
import com.zorbeytorunoglu.multiBot.messages.MessagesHandler
import com.zorbeytorunoglu.multiBot.permissions.PermissionManager
import com.zorbeytorunoglu.multiBot.settings.SettingsHandler
import com.zorbeytorunoglu.multiBot.ticket.TicketHandler
import com.zorbeytorunoglu.multiBot.ticket.configuration.TicketConfigurationHandler
import net.dv8tion.jda.api.JDABuilder

fun main() {

    val permissionManager = PermissionManager()
    val settingsHandler = SettingsHandler()
    val commandsConfigurationHandler = CommandsConfigurationHandler()

    val builder = JDABuilder.createDefault(settingsHandler.settings.token)

    val bot = Bot()

    bot.jda = builder.build()

    bot.messagesHandler = MessagesHandler()
    bot.commandsConfigurationHandler = commandsConfigurationHandler
    bot.permissionManager = permissionManager
    bot.commandsManager = CommandsManager(bot)
    bot.settingsHandler = settingsHandler
    bot.ticketHandler = TicketHandler(TicketConfigurationHandler())

    bot.jda.addEventListener(Listener(bot))

}