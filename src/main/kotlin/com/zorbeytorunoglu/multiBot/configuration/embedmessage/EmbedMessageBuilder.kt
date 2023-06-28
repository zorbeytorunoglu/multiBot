package com.zorbeytorunoglu.multiBot.configuration.embedmessage

import com.zorbeytorunoglu.multiBot.utils.Utils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member

class EmbedMessageBuilder(val config: EmbedMessageConfig): EmbedBuilder() {

    init {

        this.setAuthor(config.author,config.authorUrl,config.authorIconUrl)
        if (config.color != null)
            this.setColor(Utils.getColor(config.color))
        this.setDescription(config.description)
        this.setTitle(config.title, config.titleUrl)
        this.setFooter(config.footer,config.footerUrl)
        this.setImage(config.image)
        this.setThumbnail(config.thumbnail)
        this.setUrl(config.url)

    }

    //TODO: Placeholders can be improved
    fun applyPlaceholderAsTag(member: Member): EmbedMessageBuilder {
        this.setDescription(if (config.description != null) config.description.replace("%member%", member.asMention) else
            config.description)
        this.setTitle(if (config.title != null) config.title.replace("%member%", member.asMention)
        else config.title, config.titleUrl)
        return this
    }

}