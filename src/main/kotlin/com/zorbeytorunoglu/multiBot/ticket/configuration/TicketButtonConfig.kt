package com.zorbeytorunoglu.multiBot.ticket.configuration

import com.zorbeytorunoglu.multiBot.configuration.button.ButtonConfig

data class TicketButtonConfig(
    val buttonConfig: ButtonConfig,
    val targetCategoryId: String
)
