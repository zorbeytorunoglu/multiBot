package com.zorbeytorunoglu.multiBot.events.listeners

import com.zorbeytorunoglu.multiBot.Bot
import com.zorbeytorunoglu.multiBot.configuration.embedmessage.EmbedMessageBuilder
import com.zorbeytorunoglu.multiBot.events.AbstractListener
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent

class JoinListener(bot: Bot): AbstractListener(bot) {

    override suspend fun onEvent(event: GenericEvent) {
        if (!bot.settingsHandler.settings.welcomeMessage) return

        if (event is GuildMemberJoinEvent)
            onJoin(event)

    }

    private fun onJoin(event: GuildMemberJoinEvent) {

        val channel = event.guild.getTextChannelById(bot.settingsHandler.settings.welcomeChannel) ?: run {
            logger.error("Welcome channel could not be found! Check your settings.json file.")
            return
        }

        val config = bot.messagesHandler.messages.welcomeEmbed

        config.title = config.title?.replace("%member%", event.member.effectiveName)
        config.description = config.description?.replace("%member%", event.member.asMention)

        val embed = EmbedMessageBuilder(config).build()

        channel.sendMessageEmbeds(embed).queue()

    }

}