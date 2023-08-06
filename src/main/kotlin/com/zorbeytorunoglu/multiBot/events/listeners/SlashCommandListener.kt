package com.zorbeytorunoglu.multiBot.events.listeners

import com.zorbeytorunoglu.multiBot.Bot
import com.zorbeytorunoglu.multiBot.events.AbstractListener
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class SlashCommandListener(bot: Bot): AbstractListener(bot) {

    override suspend fun onEvent(event: GenericEvent) {

        if (event is SlashCommandInteractionEvent)
            onSlashCommand(event)

    }

    fun onSlashCommand(event: SlashCommandInteractionEvent) {
        for (command in bot.commandsManager.commands) {
            if (event.name == command.name) {
                if (!event.isFromGuild && command.guildOnly) continue
                command.execute(event)
            }
        }
    }

}