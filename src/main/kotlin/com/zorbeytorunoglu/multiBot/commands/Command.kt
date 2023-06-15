package com.zorbeytorunoglu.multiBot.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

interface Command {

    val name: String
    val description: String
    val guildOnly: Boolean
    fun optionData(): Collection<OptionData>
    fun subcommandData(): Collection<SubcommandData>

    fun execute(event: SlashCommandInteractionEvent)

}