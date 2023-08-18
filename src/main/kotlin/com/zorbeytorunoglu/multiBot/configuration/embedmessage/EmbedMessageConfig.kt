package com.zorbeytorunoglu.multiBot.configuration.embedmessage

data class EmbedMessageConfig(
    var title: String? = null,
    var titleUrl: String? = null,
    var description: String? = null,
    var author: String? = null,
    var authorUrl: String? = null,
    var authorIconUrl: String? = null,
    var color: String? = "ORANGE",
    var footer: String? = null,
    var footerUrl: String? = null,
    var image: String? = null,
    var thumbnail: String? = null,
    var url: String? = null
)
