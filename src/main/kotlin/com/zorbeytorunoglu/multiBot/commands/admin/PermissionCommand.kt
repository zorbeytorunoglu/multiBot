package com.zorbeytorunoglu.multiBot.commands.admin

import com.zorbeytorunoglu.multiBot.commands.Command
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

class PermissionCommand: Command {

    //TODO: /permission give/remove/check <member,role> <permission>

    override val name: String
        get() = TODO("Not yet implemented")
    override val description: String
        get() = TODO("Not yet implemented")
    override val guildOnly: Boolean
        get() = TODO("Not yet implemented")

    override fun optionData(): Collection<OptionData> {
        TODO("Not yet implemented")
    }

    override fun subcommandData(): Collection<SubcommandData> {
        TODO("Not yet implemented")
    }

    override fun execute(event: SlashCommandInteractionEvent) {
        TODO("Not yet implemented")
    }

}