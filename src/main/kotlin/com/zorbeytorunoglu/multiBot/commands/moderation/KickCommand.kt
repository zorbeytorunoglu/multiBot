package com.zorbeytorunoglu.multiBot.commands.moderation

import com.zorbeytorunoglu.multiBot.Bot
import com.zorbeytorunoglu.multiBot.commands.Command
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

class KickCommand(private val bot: Bot): Command {
    override val name: String
        get() = bot.commandsConfigurationHandler.commands.kickCmd
    override val description: String
        get() = bot.commandsConfigurationHandler.commands.kickDesc
    override val guildOnly: Boolean
        get() = true

    override fun optionData(): Collection<OptionData> {
        return listOf(
            OptionData(OptionType.USER, "member","Member you want to kick.", true, false),
            OptionData(OptionType.STRING, "id", "ID of the member you want to kick.", true, false)
        )
    }

    override fun subcommandData(): Collection<SubcommandData> {
        return emptyList()
    }

    override fun execute(event: SlashCommandInteractionEvent) {

        if (!event.member!!.permissions.contains(Permission.KICK_MEMBERS)) {
            event.reply(bot.messagesHandler.messages.noPermission).setEphemeral(true).queue()
            return
        }

        println("hi")

        //TODO: Complete

    }


}