package com.zorbeytorunoglu.multiBot.commands.misc

import com.zorbeytorunoglu.multiBot.Bot
import com.zorbeytorunoglu.multiBot.commands.Command
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData


class PingCommand(private val bot: Bot): Command {

    override val name: String
        get() = bot.commandsConfigurationHandler.commands.pingCmd
    override val description: String
        get() = bot.commandsConfigurationHandler.commands.pingDesc

    override val guildOnly: Boolean
        get() = false

    override fun optionData(): Collection<OptionData> {
        return emptyList()
    }

    override fun subcommandData(): Collection<SubcommandData> {
        return emptyList()
    }

    override fun execute(event: SlashCommandInteractionEvent) {

        event.reply("Pong").queue()

    }

}