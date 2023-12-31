package com.zorbeytorunoglu.multiBot

import com.zorbeytorunoglu.multiBot.commands.CommandsManager
import com.zorbeytorunoglu.multiBot.commands.configuration.CommandsConfigurationHandler
import com.zorbeytorunoglu.multiBot.messages.MessagesHandler
import com.zorbeytorunoglu.multiBot.permissions.PermissionManager
import com.zorbeytorunoglu.multiBot.settings.SettingsHandler
import com.zorbeytorunoglu.multiBot.task.TaskManager
import com.zorbeytorunoglu.multiBot.ticket.TicketManager
import net.dv8tion.jda.api.JDA

class Bot {

    lateinit var jda: JDA
    lateinit var settingsHandler: SettingsHandler
    lateinit var permissionManager: PermissionManager
    lateinit var commandsConfigurationHandler: CommandsConfigurationHandler
    lateinit var commandsManager: CommandsManager
    lateinit var ticketHandler: TicketManager
    lateinit var messagesHandler: MessagesHandler
    lateinit var taskManager: TaskManager

}