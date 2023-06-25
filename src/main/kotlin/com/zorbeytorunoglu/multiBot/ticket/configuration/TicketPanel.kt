package com.zorbeytorunoglu.multiBot.ticket.configuration

import com.zorbeytorunoglu.multiBot.configuration.embedmessage.EmbedMessage
import net.dv8tion.jda.api.entities.MessageEmbed

class TicketPanel(val ticketPanelConfig: TicketPanelConfig) {

    val embedMessage: MessageEmbed
    val buttons: List<TicketButton>

    init {

        embedMessage = EmbedMessage(ticketPanelConfig.embedMessageConfig).embedMessage

        val buttons: MutableList<TicketButton> = mutableListOf()

        ticketPanelConfig.buttonsConfig.forEach {
            buttons.add(TicketButton(it))
        }

        this.buttons = buttons.toList()

    }

}