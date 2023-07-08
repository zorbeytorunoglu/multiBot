package com.zorbeytorunoglu.multiBot.commands

import com.zorbeytorunoglu.multiBot.Bot
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class Listener(private val bot: Bot): ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {

        for (command in bot.commandsManager.commands) {

            if (event.name == command.name) {

                if (!event.isFromGuild && command.guildOnly) continue
                command.execute(event)

            }

        }

    }

}