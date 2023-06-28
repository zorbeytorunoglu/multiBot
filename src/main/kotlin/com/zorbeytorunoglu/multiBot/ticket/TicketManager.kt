package com.zorbeytorunoglu.multiBot.ticket

import com.zorbeytorunoglu.multiBot.ticket.configuration.TicketButton
import com.zorbeytorunoglu.multiBot.ticket.configuration.TicketConfigurationHandler
import com.zorbeytorunoglu.multiBot.ticket.configuration.TicketPanel
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.interactions.components.buttons.Button

class TicketManager(val ticketConfigurationHandler: TicketConfigurationHandler) {

    fun isTicketPanelButton(button: Button): Boolean {

        if (ticketConfigurationHandler.ticketPanels.isEmpty()) return false

        ticketConfigurationHandler.ticketPanels.values.forEach { ticketPanel ->
            ticketPanel.buttons.forEach {
                if (it.button.id == button.id) return true
            }
        }

        return false

    }

    fun getTicketPanel(button: Button): TicketPanel? {

        if (ticketConfigurationHandler.ticketPanels.isEmpty()) return null

        ticketConfigurationHandler.ticketPanels.values.forEach { ticketPanel ->
            ticketPanel.buttons.forEach {
                if (it.button.id == button.id) return ticketPanel
            }
        }

        return null

    }

    fun getTicketButton(button: Button): TicketButton? {

        if (ticketConfigurationHandler.ticketPanels.isEmpty()) return null

        ticketConfigurationHandler.ticketPanels.values.forEach { ticketPanel ->
            ticketPanel.buttons.forEach {
                if (it.button.id == button.id) return it
            }
        }

        return null

    }

    fun getMemberTickets(member: Member): Collection<TextChannel> {

        val tickets = mutableListOf<TextChannel>()

        if (member.guild.textChannels.isEmpty()) return tickets

        member.guild.textChannels.forEach {
            if (isMembersTicket(member, it))
                tickets.add(it)
        }

        return tickets

    }

    fun isMembersTicket(member: Member, textChannel: TextChannel): Boolean {

        if (textChannel.topic == null) return false

        val args = textChannel.topic!!.split("-")

        if (args.size != 3) return false

        return args[1] == member.id

    }

    fun getTicketLimit(buttonId: String): Int {

        if (ticketConfigurationHandler.ticketPanels.isEmpty()) return 0

        for (panel in ticketConfigurationHandler.ticketPanels.values) {
            if (panel.buttons.isEmpty()) continue
            for (button in panel.buttons) {
                if (button.button.id == buttonId) {
                    return button.ticketButtonConfig.limit
                }
            }
        }

        return 0

    }

    fun exceededTicketLimit(member: Member, buttonId: String): Boolean {
        return getTicketLimit(buttonId) <= getMemberTickets(member).size
    }

}