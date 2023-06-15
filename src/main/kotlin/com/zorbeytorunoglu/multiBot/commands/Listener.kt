package com.zorbeytorunoglu.multiBot.commands

import com.zorbeytorunoglu.multiBot.Bot
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class Listener(private val bot: Bot): ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {

        bot.commandsManager.commands.forEach {
            if (it.name == event.name) it.execute(event)
        }

    }

}