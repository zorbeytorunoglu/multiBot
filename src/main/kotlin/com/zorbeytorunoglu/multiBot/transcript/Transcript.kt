package com.zorbeytorunoglu.multiBot.transcript

import com.zorbeytorunoglu.multiBot.transcript.Formatter.format
import com.zorbeytorunoglu.multiBot.transcript.Formatter.formatBytes
import com.zorbeytorunoglu.multiBot.transcript.Formatter.toHex
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.utils.FileUpload
import org.jsoup.Jsoup
import java.io.*
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

class Transcript {
    private val imageFormats: List<String?> = mutableListOf<String?>("png", "jpg", "jpeg", "gif")
    private val videoFormats: List<String?> =
        mutableListOf<String?>("mp4", "webm", "mkv", "avi", "mov", "flv", "wmv", "mpg", "mpeg")
    private val audioFormats: List<String?> = mutableListOf<String?>("mp3", "wav", "ogg", "flac")
    @JvmOverloads
    @Throws(IOException::class)
    fun createTranscript(messageChannel: MessageChannel, textChannel: TextChannel, filename: String? = null) {
        textChannel.sendFiles(FileUpload.fromData(this.createTranscript(messageChannel), filename ?: "transcript.html"))
            .queue()
    }

    @Throws(IOException::class)
    fun createTranscript(channel: MessageChannel): InputStream {
        return fromMessages(channel.iterableHistory.stream().collect(Collectors.toList()))
    }

    @Throws(IOException::class)
    fun fromMessages(messages: Collection<Message>): InputStream {
        val htmlTemplate = findFile("template.html")
        require(!messages.isEmpty()) { "No messages to generate a transcript from" }
        val channel: Channel = messages.iterator().next().channel
        val document = Jsoup.parse(htmlTemplate, "UTF-8")
        document.outputSettings().indentAmount(0).prettyPrint(true)
        (if (channel.type == ChannelType.PRIVATE) (channel as PrivateChannel).user!!.effectiveAvatarUrl else (channel as GuildChannel).guild.iconUrl)?.let {
            document.getElementsByClass("preamble__guild-icon")
                .first()!!.attr(
                    "src",
                    it
                )
        } // set guild icon
        document.getElementById("transcriptTitle")!!.text(channel.name) // set title
        document.getElementById("guildname")!!.text(
            if (channel.type == ChannelType.PRIVATE) (channel as PrivateChannel).user!!.name else (channel as GuildChannel).guild.name
        ) // set guild name
        document.getElementById("ticketname")!!.text(channel.name) // set channel name
        val chatLog = document.getElementById("chatlog") // chat log
        for (message in messages.stream()
            .sorted(Comparator.comparing { obj: Message -> obj.timeCreated })
            .collect(Collectors.toList<Message>())) {
            val messageGroup = document.createElement("div")
            messageGroup.addClass("chatlog__message-group")
            if (message.referencedMessage != null) {
                val referenceSymbol = document.createElement("div")
                referenceSymbol.addClass("chatlog__reference-symbol")
                val reference = document.createElement("div")
                reference.addClass("chatlog__reference")
                val referenceMessage = message.referencedMessage
                val author = referenceMessage!!.author
                val color: String = if (channel.type == ChannelType.PRIVATE) "#FFFFFF" else {
                    val member = (channel as GuildChannel).guild.getMember(author)
                    if (member == null) "#FFFFFF" else if (member.color == null) "#FFFFFF" else toHex(
                        member.color!!
                    )
                }
                val referenceContent = document.createElement("div")
                val referenceAuthor = document.createElement("img")
                referenceAuthor.addClass("chatlog__reference-avatar")
                referenceAuthor.attr("src", author.effectiveAvatarUrl)
                referenceAuthor.attr("alt", "Avatar")
                referenceAuthor.attr("loading", "lazy")
                val referenceAuthorName = document.createElement("span")
                referenceAuthorName.addClass("chatlog__reference-name")
                referenceAuthorName.attr("title", author.name)
                referenceAuthorName.attr("style", "color: $color")
                referenceAuthorName.text(author.name)
                val referenceContentContent = document.createElement("div")
                referenceContentContent.addClass("chatlog__reference-content")
                val referenceContentContentText = document.createElement("span")
                referenceContentContentText.addClass("chatlog__reference-link")
                referenceContentContentText.attr("onclick", "scrollToMessage(event, '" + referenceMessage.id + "')")
                val referenceEm = document.createElement("em")
                referenceEm.text(
                    if (referenceMessage.contentDisplay.length > 42) (referenceMessage.contentDisplay.substring(
                        0,
                        42
                    )
                            + "...") else referenceMessage.contentDisplay
                )
                referenceContentContentText.appendChild(referenceEm)
                referenceContentContent.appendChild(referenceContentContentText)
                referenceContent.appendChild(referenceAuthor)
                referenceContent.appendChild(referenceAuthorName)
                referenceContent.appendChild(referenceContentContent)
                reference.appendChild(referenceContent)
                messageGroup.appendChild(referenceSymbol)
                messageGroup.appendChild(reference)
            }
            val author = message.author
            val authorElement = document.createElement("div")
            authorElement.addClass("chatlog__author-avatar-container")
            val authorAvatar = document.createElement("img")
            authorAvatar.addClass("chatlog__author-avatar")
            authorAvatar.attr("src", author.effectiveAvatarUrl)
            authorAvatar.attr("alt", "Avatar")
            authorAvatar.attr("loading", "lazy")
            authorElement.appendChild(authorAvatar)
            messageGroup.appendChild(authorElement)
            val content = document.createElement("div")
            content.addClass("chatlog__messages")
            val authorName = document.createElement("span")
            authorName.addClass("chatlog__author-name")
            authorName.attr("title", author.asMention)
            authorName.text(author.name)
            authorName.attr("data-user-id", author.id)
            content.appendChild(authorName)
            if (author.isBot) {
                val botTag = document.createElement("span")
                botTag.addClass("chatlog__bot-tag").text("BOT")
                content.appendChild(botTag)
            }
            val timestamp = document.createElement("span")
            timestamp.addClass("chatlog__timestamp")
            timestamp
                .text(message.timeCreated.format(DateTimeFormatter.ofPattern("HH:mm:ss")))
            content.appendChild(timestamp)
            val messageContent = document.createElement("div")
            messageContent.addClass("chatlog__message")
            messageContent.attr("data-message-id", message.id)
            messageContent.attr("id", "message-" + message.id)
            messageContent.attr(
                "title", "Message sent: "
                        + message.timeCreated.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            )
            if (message.contentDisplay.isNotEmpty()) {
                val messageContentContent = document.createElement("div")
                messageContentContent.addClass("chatlog__content")
                val messageContentContentMarkdown = document.createElement("div")
                messageContentContentMarkdown.addClass("markdown")
                val messageContentContentMarkdownSpan = document.createElement("span")
                messageContentContentMarkdownSpan.addClass("preserve-whitespace")
                messageContentContentMarkdownSpan
                    .html(format(message.contentDisplay))
                messageContentContentMarkdown.appendChild(messageContentContentMarkdownSpan)
                messageContentContent.appendChild(messageContentContentMarkdown)
                messageContent.appendChild(messageContentContent)
                if (message.reactions.size > 0) {
                    val reactionsContent = document.createElement("div")
                    reactionsContent.addClass("chatlog__reactions")
                    for (reaction in message.reactions) {
                        val reactionContent = document.createElement("div")
                        reactionContent.addClass("chatlog__reaction")
                        reactionContent.attr("data-reaction-id", reaction.emoji.asReactionCode)
                        reactionContent.attr("data-reaction-name", reaction.emoji.name)
                        reactionContent.attr("data-reaction-count", reaction.count.toString())
                        reactionContent.attr("data-reaction-me", reaction.isSelf.toString())
                        reactionContent.html(reaction.emoji.formatted)
                        val reactionContentCount = document.createElement("div")
                        reactionContentCount.addClass("chatlog__reaction-count")
                        reactionContentCount.text(reaction.count.toString())
                        reactionContent.appendChild(reactionContentCount)
                        reactionsContent.appendChild(reactionContent)
                        messageContentContent.appendChild(reactionsContent)
                    }
                }
            }
            if (message.attachments.isNotEmpty()) for (attach in message.attachments) {
                val attachmentsDiv = document.createElement("div")
                attachmentsDiv.addClass("chatlog__attachment")
                val attachmentType = attach.fileExtension
                if (imageFormats.contains(attachmentType)) {
                    val attachmentLink = document.createElement("a")
                    val attachmentImage = document.createElement("img")
                    attachmentImage.addClass("chatlog__attachment-media")
                    attachmentImage.attr("src", attach.url)
                    attachmentImage.attr("alt", "Image attachment")
                    attachmentImage.attr("loading", "lazy")
                    attachmentImage.attr(
                        "title",
                        "Image: " + attach.fileName + formatBytes(attach.size.toLong())
                    )
                    attachmentLink.appendChild(attachmentImage)
                    attachmentsDiv.appendChild(attachmentLink)
                } else if (videoFormats.contains(attachmentType)) {
                    val attachmentVideo = document.createElement("video")
                    attachmentVideo.addClass("chatlog__attachment-media")
                    attachmentVideo.attr("src", attach.url)
                    attachmentVideo.attr("alt", "Video attachment")
                    attachmentVideo.attr("controls", true)
                    attachmentVideo.attr(
                        "title",
                        "Video: " + attach.fileName + formatBytes(attach.size.toLong())
                    )
                    attachmentsDiv.appendChild(attachmentVideo)
                } else if (audioFormats.contains(attachmentType)) {
                    val attachmentAudio = document.createElement("audio")
                    attachmentAudio.addClass("chatlog__attachment-media")
                    attachmentAudio.attr("src", attach.url)
                    attachmentAudio.attr("alt", "Audio attachment")
                    attachmentAudio.attr("controls", true)
                    attachmentAudio.attr(
                        "title",
                        "Audio: " + attach.fileName + formatBytes(attach.size.toLong())
                    )
                    attachmentsDiv.appendChild(attachmentAudio)
                } else {
                    val attachmentGeneric = document.createElement("div")
                    attachmentGeneric.addClass("chatlog__attachment-generic")
                    val attachmentGenericIcon = document.createElement("svg")
                    attachmentGenericIcon.addClass("chatlog__attachment-generic-icon")
                    val attachmentGenericIconUse = document.createElement("use")
                    attachmentGenericIconUse.attr("xlink:href", "#icon-attachment")
                    attachmentGenericIcon.appendChild(attachmentGenericIconUse)
                    attachmentGeneric.appendChild(attachmentGenericIcon)
                    val attachmentGenericName = document.createElement("div")
                    attachmentGenericName.addClass("chatlog__attachment-generic-name")
                    val attachmentGenericNameLink = document.createElement("a")
                    attachmentGenericNameLink.attr("href", attach.url)
                    attachmentGenericNameLink.text(attach.fileName)
                    attachmentGenericName.appendChild(attachmentGenericNameLink)
                    attachmentGeneric.appendChild(attachmentGenericName)
                    val attachmentGenericSize = document.createElement("div")
                    attachmentGenericSize.addClass("chatlog__attachment-generic-size")
                    attachmentGenericSize.text(formatBytes(attach.size.toLong()))
                    attachmentGeneric.appendChild(attachmentGenericSize)
                    attachmentsDiv.appendChild(attachmentGeneric)
                }
                messageContent.appendChild(attachmentsDiv)
            }
            content.appendChild(messageContent)
            if (message.embeds.isNotEmpty()) for (embed in message.embeds) {
                if (embed == null) continue
                val embedDiv = document.createElement("div")
                embedDiv.addClass("chatlog__embed")
                if (embed.color != null) {
                    val embedColorPill = document.createElement("div")
                    embedColorPill.addClass("chatlog__embed-color-pill")
                    embedColorPill.attr(
                        "style",
                        "background-color: #" + toHex(embed.color!!)
                    )
                    embedDiv.appendChild(embedColorPill)
                }
                val embedContentContainer = document.createElement("div")
                embedContentContainer.addClass("chatlog__embed-content-container")
                val embedContent = document.createElement("div")
                embedContent.addClass("chatlog__embed-content")
                val embedText = document.createElement("div")
                embedText.addClass("chatlog__embed-text")
                if (embed.author != null && embed.author!!.name != null) {
                    val embedAuthor = document.createElement("div")
                    embedAuthor.addClass("chatlog__embed-author")
                    if (embed.author!!.iconUrl != null) {
                        val embedAuthorIcon = document.createElement("img")
                        embedAuthorIcon.addClass("chatlog__embed-author-icon")
                        embed.author!!.iconUrl?.let { embedAuthorIcon.attr("src", it) }
                        embedAuthorIcon.attr("alt", "Author icon")
                        embedAuthorIcon.attr("loading", "lazy")
                        embedAuthor.appendChild(embedAuthorIcon)
                    }
                    val embedAuthorName = document.createElement("span")
                    embedAuthorName.addClass("chatlog__embed-author-name")
                    if (embed.author!!.url != null) {
                        val embedAuthorNameLink = document.createElement("a")
                        embedAuthorNameLink.addClass("chatlog__embed-author-name-link")
                        embed.author!!.url?.let { embedAuthorNameLink.attr("href", it) }
                        embed.author!!.name?.let { embedAuthorNameLink.text(it) }
                        embedAuthorName.appendChild(embedAuthorNameLink)
                    } else embed.author!!.name?.let { embedAuthorName.text(it) }
                    embedAuthor.appendChild(embedAuthorName)
                    embedText.appendChild(embedAuthor)
                }
                if (embed.title != null) {
                    val embedTitle = document.createElement("div")
                    embedTitle.addClass("chatlog__embed-title")
                    if (embed.url != null) {
                        val embedTitleLink = document.createElement("a")
                        embedTitleLink.addClass("chatlog__embed-title-link")
                        embed.url?.let { embedTitleLink.attr("href", it) }
                        val embedTitleMarkdown = document.createElement("div")
                        embedTitleMarkdown.addClass("markdown preserve-whitespace")
                            .html(format(embed.title!!))
                        embedTitleLink.appendChild(embedTitleMarkdown)
                        embedTitle.appendChild(embedTitleLink)
                    } else {
                        val embedTitleMarkdown = document.createElement("div")
                        embedTitleMarkdown.addClass("markdown preserve-whitespace")
                            .html(format(embed.title!!))
                        embedTitle.appendChild(embedTitleMarkdown)
                    }
                    embedText.appendChild(embedTitle)
                }
                if (embed.description != null) {
                    val embedDescription = document.createElement("div")
                    embedDescription.addClass("chatlog__embed-description")
                    val embedDescriptionMarkdown = document.createElement("div")
                    embedDescriptionMarkdown.addClass("markdown preserve-whitespace")
                    embedDescriptionMarkdown
                        .html(format(embed.description!!))
                    embedDescription.appendChild(embedDescriptionMarkdown)
                    embedText.appendChild(embedDescription)
                }
                if (embed.fields.isNotEmpty()) {
                    val embedFields = document.createElement("div")
                    embedFields.addClass("chatlog__embed-fields")
                    for (field in embed.fields) {
                        val embedField = document.createElement("div")
                        embedField.addClass(if (field.isInline) "chatlog__embed-field-inline" else "chatlog__embed-field")
                        val embedFieldName = document.createElement("div")
                        embedFieldName.addClass("chatlog__embed-field-name")
                        val embedFieldNameMarkdown = document.createElement("div")
                        embedFieldNameMarkdown.addClass("markdown preserve-whitespace")
                        field.name?.let { embedFieldNameMarkdown.html(it) }
                        embedFieldName.appendChild(embedFieldNameMarkdown)
                        embedField.appendChild(embedFieldName)
                        val embedFieldValue = document.createElement("div")
                        embedFieldValue.addClass("chatlog__embed-field-value")
                        val embedFieldValueMarkdown = document.createElement("div")
                        embedFieldValueMarkdown.addClass("markdown preserve-whitespace")
                        embedFieldValueMarkdown
                            .html(format(field.value!!))
                        embedFieldValue.appendChild(embedFieldValueMarkdown)
                        embedField.appendChild(embedFieldValue)
                        embedFields.appendChild(embedField)
                    }
                    embedText.appendChild(embedFields)
                }
                embedContent.appendChild(embedText)
                if (embed.thumbnail != null) {
                    val embedThumbnail = document.createElement("div")
                    embedThumbnail.addClass("chatlog__embed-thumbnail-container")
                    val embedThumbnailLink = document.createElement("a")
                    embedThumbnailLink.addClass("chatlog__embed-thumbnail-link")
                    embed.thumbnail!!.url?.let { embedThumbnailLink.attr("href", it) }
                    val embedThumbnailImage = document.createElement("img")
                    embedThumbnailImage.addClass("chatlog__embed-thumbnail")
                    embed.thumbnail!!.url?.let { embedThumbnailImage.attr("src", it) }
                    embedThumbnailImage.attr("alt", "Thumbnail")
                    embedThumbnailImage.attr("loading", "lazy")
                    embedThumbnailLink.appendChild(embedThumbnailImage)
                    embedThumbnail.appendChild(embedThumbnailLink)
                    embedContent.appendChild(embedThumbnail)
                }
                embedContentContainer.appendChild(embedContent)
                if (embed.image != null) {
                    val embedImage = document.createElement("div")
                    embedImage.addClass("chatlog__embed-image-container")
                    val embedImageLink = document.createElement("a")
                    embedImageLink.addClass("chatlog__embed-image-link")
                    embed.image!!.url?.let { embedImageLink.attr("href", it) }
                    val embedImageImage = document.createElement("img")
                    embedImageImage.addClass("chatlog__embed-image")
                    embed.image!!.url?.let { embedImageImage.attr("src", it) }
                    embedImageImage.attr("alt", "Image")
                    embedImageImage.attr("loading", "lazy")
                    embedImageLink.appendChild(embedImageImage)
                    embedImage.appendChild(embedImageLink)
                    embedContentContainer.appendChild(embedImage)
                }
                if (embed.footer != null) {
                    val embedFooter = document.createElement("div")
                    embedFooter.addClass("chatlog__embed-footer")
                    if (embed.footer!!.iconUrl != null) {
                        val embedFooterIcon = document.createElement("img")
                        embedFooterIcon.addClass("chatlog__embed-footer-icon")
                        embed.footer!!.iconUrl?.let { embedFooterIcon.attr("src", it) }
                        embedFooterIcon.attr("alt", "Footer icon")
                        embedFooterIcon.attr("loading", "lazy")
                        embedFooter.appendChild(embedFooterIcon)
                    }
                    val embedFooterText = document.createElement("span")
                    embedFooterText.addClass("chatlog__embed-footer-text")
                    (if (embed.timestamp != null) embed.footer!!.text + " • " + embed.timestamp!!
                        .format(DateTimeFormatter.ofPattern("HH:mm:ss")) else embed.footer!!.text)?.let {
                        embedFooterText.text(
                            it
                        )
                    }
                    embedFooter.appendChild(embedFooterText)
                    embedContentContainer.appendChild(embedFooter)
                }
                embedDiv.appendChild(embedContentContainer)
                content.appendChild(embedDiv)
            }
            messageGroup.appendChild(content)
            chatLog!!.appendChild(messageGroup)
        }
        return ByteArrayInputStream(document.outerHtml().toByteArray())
    }

    @Throws(FileNotFoundException::class)
    private fun findFile(fileName: String): File {
        val file = File("./$fileName")
        if (!file.exists()) throw FileNotFoundException("File not found: $fileName")
        return file
    }
}