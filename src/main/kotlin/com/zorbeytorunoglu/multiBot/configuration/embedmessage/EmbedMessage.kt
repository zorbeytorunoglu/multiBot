package com.zorbeytorunoglu.multiBot.configuration.embedmessage

import com.zorbeytorunoglu.multiBot.utils.Utils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed

class EmbedMessage(config: EmbedMessageConfig) {

    val embedMessage: MessageEmbed

    init {

        val builder = EmbedBuilder()

        builder.setAuthor(config.author,config.authorUrl,config.authorIconUrl)
        if (config.color != null)
            builder.setColor(Utils.getColor(config.color))
        builder.setDescription(config.description)
        builder.setTitle(config.title, config.titleUrl)
        builder.setFooter(config.footer,config.footerUrl)
        builder.setImage(config.image)
        builder.setThumbnail(config.thumbnail)
        builder.setUrl(config.url)

        embedMessage = builder.build()

    }

}