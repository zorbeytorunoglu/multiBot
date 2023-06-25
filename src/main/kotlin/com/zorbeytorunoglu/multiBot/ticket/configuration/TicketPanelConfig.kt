package com.zorbeytorunoglu.multiBot.ticket.configuration

import com.zorbeytorunoglu.multiBot.configuration.embedmessage.EmbedMessageConfig

data class TicketPanelConfig(
    val titleFormat: String,
    val embedMessageConfig: EmbedMessageConfig,
    val buttonsConfig: List<TicketButtonConfig>,
    val ticketCategory: String,
    val openingEmbed: EmbedMessageConfig
)