package com.zorbeytorunoglu.multiBot.configuration.embedmessage

data class EmbedMessageConfig(
    val title: String? = "example",
    val titleUrl: String? = null,
    val description: String? = "example",
    val author: String? = "example",
    val authorUrl: String? = null,
    val authorIconUrl: String? = null,
    val color: String? = "ORANGE",
    val footer: String? = "example",
    val footerUrl: String? = null,
    val image: String? = null,
    val thumbnail: String? = null,
    val url: String? = null
)
