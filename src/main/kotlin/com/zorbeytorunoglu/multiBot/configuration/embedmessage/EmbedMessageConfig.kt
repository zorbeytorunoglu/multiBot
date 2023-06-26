package com.zorbeytorunoglu.multiBot.configuration.embedmessage

data class EmbedMessageConfig(
    val title: String? = null,
    val titleUrl: String? = null,
    val description: String? = null,
    val author: String? = null,
    val authorUrl: String? = null,
    val authorIconUrl: String? = null,
    val color: String? = "ORANGE",
    val footer: String? = null,
    val footerUrl: String? = null,
    val image: String? = null,
    val thumbnail: String? = null,
    val url: String? = null
)
