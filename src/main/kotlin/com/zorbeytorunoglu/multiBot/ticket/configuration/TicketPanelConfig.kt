package com.zorbeytorunoglu.multiBot.ticket.configuration

import com.zorbeytorunoglu.multiBot.configuration.embedmessage.EmbedMessageConfig

data class TicketPanelConfig(
    val embedMessageConfig: EmbedMessageConfig,
    val buttonConfigs: List<TicketButtonConfig>
)