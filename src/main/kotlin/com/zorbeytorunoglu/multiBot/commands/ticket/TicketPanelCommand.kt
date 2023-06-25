package com.zorbeytorunoglu.multiBot.commands.ticket

import com.zorbeytorunoglu.multiBot.Bot
import com.zorbeytorunoglu.multiBot.commands.Command
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

class TicketPanelCommand(private val bot: Bot): Command {
    override val name: String
        get() = bot.commandsConfigurationHandler.commands.ticketPanelCmd
    override val description: String
        get() = bot.commandsConfigurationHandler.commands.ticketPanelDesc
    override val guildOnly: Boolean
        get() = true

    override fun optionData(): Collection<OptionData> {

        return listOf(OptionData(OptionType.STRING, "id", bot.messagesHandler.messages.ticketPanelIdOption, true, false))

    }

    override fun subcommandData(): Collection<SubcommandData> {
        return emptyList()
    }

    override fun execute(event: SlashCommandInteractionEvent) {

        val id = event.getOption("id")!!.asString

        val ticketPanel = bot.ticketHandler.ticketConfigurationHandler.ticketPanels[id]!!

        val buttons = ticketPanel.buttons.map { it.button.button }

        event.channel.sendMessageEmbeds(ticketPanel.embedMessage)
            .addActionRow(buttons).queue {
                println("Ticket panel is sent!")
                event.reply(bot.messagesHandler.messages.ticketPanelSent).setEphemeral(true).queue()
            }

        //TODO: Make usable ticket panels in ticketPanels.json

    }


}