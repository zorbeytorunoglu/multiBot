package com.zorbeytorunoglu.multiBot.ticket

import com.zorbeytorunoglu.multiBot.configuration.embedmessage.EmbedMessageConfig
import com.zorbeytorunoglu.multiBot.ticket.configuration.TicketButtonConfig

data class TicketPanelConfig(
    val titleFormat: String,
    val embedMessageConfig: EmbedMessageConfig,
    val buttonsConfig: List<TicketButtonConfig>,
    val ticketCategory: String,
    val openingEmbed: EmbedMessageConfig
)