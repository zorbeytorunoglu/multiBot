package com.zorbeytorunoglu.multiBot.ticket.configuration

import com.zorbeytorunoglu.multiBot.configuration.button.Button

class TicketButton(val ticketButtonConfig: TicketButtonConfig) {

    val button = Button(ticketButtonConfig.buttonConfig)
    val targetCategoryId = ticketButtonConfig.targetCategoryId

}