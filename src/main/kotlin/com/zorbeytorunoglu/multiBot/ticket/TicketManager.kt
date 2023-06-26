package com.zorbeytorunoglu.multiBot.ticket

import com.zorbeytorunoglu.multiBot.ticket.configuration.TicketButton
import com.zorbeytorunoglu.multiBot.ticket.configuration.TicketConfigurationHandler
import com.zorbeytorunoglu.multiBot.ticket.configuration.TicketPanel
import net.dv8tion.jda.api.interactions.components.buttons.Button

class TicketManager(val ticketConfigurationHandler: TicketConfigurationHandler) {

    fun isTicketPanelButton(button: Button): Boolean {

        ticketConfigurationHandler.ticketPanels.values.forEach { ticketPanel ->
            ticketPanel.buttons.forEach {
                if (it.button.id == button.id) return true
            }
        }

        return false

    }

    fun getTicketPanel(button: Button): TicketPanel? {

        ticketConfigurationHandler.ticketPanels.values.forEach { ticketPanel ->
            ticketPanel.buttons.forEach {
                if (it.button.id == button.id) return ticketPanel
            }
        }

        return null

    }

    fun getTicketButton(button: Button): TicketButton? {

        ticketConfigurationHandler.ticketPanels.values.forEach { ticketPanel ->
            ticketPanel.buttons.forEach {
                if (it.button.id == button.id) return it
            }
        }

        return null

    }

}