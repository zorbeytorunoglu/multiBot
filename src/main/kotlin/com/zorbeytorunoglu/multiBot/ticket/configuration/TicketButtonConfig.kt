package com.zorbeytorunoglu.multiBot.ticket.configuration

import com.zorbeytorunoglu.multiBot.configuration.button.ButtonConfig
import com.zorbeytorunoglu.multiBot.configuration.embedmessage.EmbedMessageConfig

data class TicketButtonConfig(
    val buttonConfig: ButtonConfig,
    val targetCategoryId: String,
    val channelTitleFormat: String,
    val openingEmbed: EmbedMessageConfig? = null,
    val rolesToBeAdded: String? = null,
    val rolesToBePinged: String? = null,
    val limit: Int = 1
)
