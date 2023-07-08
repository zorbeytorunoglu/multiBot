package com.zorbeytorunoglu.multiBot.transcript;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.URL;;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class DiscordHtmlTranscripts {

    private final List<String>
            imageFormats = Arrays.asList("png", "jpg", "jpeg", "gif"),
            videoFormats = Arrays.asList("mp4", "webm", "mkv", "avi", "mov", "flv", "wmv", "mpg", "mpeg"),
            audioFormats = Arrays.asList("mp3", "wav", "ogg", "flac");


    private static final DiscordHtmlTranscripts instance = new DiscordHtmlTranscripts();

    public static DiscordHtmlTranscripts getInstance() {
        return instance;
    }

    public void createTranscript(net.dv8tion.jda.api.entities.channel.concrete.TextChannel channel) throws IOException {
        createTranscript(channel, null);
    }

    public void createTranscript(net.dv8tion.jda.api.entities.channel.concrete.TextChannel channel, String fileName) throws IOException {
        channel.sendFiles(FileUpload.fromData(
                generateFromMessages(channel.getIterableHistory().stream().collect(Collectors.toList())), fileName != null ? fileName : "transcript.html")
        ).queue();
    }

    public InputStream generateFromMessages(Collection<Message> messages) throws IOException {
        File htmlTemplate = findFile("template.html");
        if (messages.isEmpty()) {
            throw new IllegalArgumentException("No messages to generate a transcript from");
        }
        TextChannel channel = messages.iterator().next().getChannel().asTextChannel();
        Document document = Jsoup.parse(htmlTemplate, "UTF-8");
        document.outputSettings().indentAmount(0).prettyPrint(true);
        document.getElementsByClass("preamble__guild-icon")
                .first().attr("src", channel.getGuild().getIconUrl()); // set guild icon

        document.getElementById("transcriptTitle").text(channel.getName()); // set title
        document.getElementById("guildname").text(channel.getGuild().getName()); // set guild name
        document.getElementById("ticketname").text(channel.getName()); // set channel name

        Element chatLog = document.getElementById("chatlog"); // chat log
        for (Message message : messages.stream()
                .sorted(Comparator.comparing(ISnowflake::getTimeCreated))
                .collect(Collectors.toList())) {
            // create message group
            Element messageGroup = document.createElement("div");
            messageGroup.addClass("chatlog__message-group");

            // message reference
            if (message.getReferencedMessage() != null) { // preguntar si es eso
                // message.reference?.messageId
                // create symbol
                Element referenceSymbol = document.createElement("div");
                referenceSymbol.addClass("chatlog__reference-symbol");

                // create reference
                Element reference = document.createElement("div");
                reference.addClass("chatlog__reference");

                Message referenceMessage = message.getReferencedMessage();
                User author = referenceMessage.getAuthor();
                Member member = channel.getGuild().getMember(author);
                String color = Formatter.toHex(Objects.requireNonNull(member.getColor()));

                //        System.out.println("REFERENCE MSG " + referenceMessage.getContentDisplay());
                reference.html("<img class=\"chatlog__reference-avatar\" src=\""
                        + author.getAvatarUrl() + "\" alt=\"Avatar\" loading=\"lazy\">" +
                        "<span class=\"chatlog__reference-name\" title=\"" + author.getName()
                        + "\" style=\"color: " + color + "\">" + author.getName() + "\"</span>" +
                        "<div class=\"chatlog__reference-content\">" +
                        " <span class=\"chatlog__reference-link\" onclick=\"scrollToMessage(event, '"
                        + referenceMessage.getId() + "')\">" +
                        "<em>" +
                        referenceMessage.getContentDisplay() != null
                        ? referenceMessage.getContentDisplay().length() > 42
                        ? referenceMessage.getContentDisplay().substring(0, 42)
                        + "..."
                        : referenceMessage.getContentDisplay()
                        : "Click to see attachment" +
                        "</em>" +
                        "</span>" +
                        "</div>");

                messageGroup.appendChild(referenceSymbol);
                messageGroup.appendChild(reference);
            }

            User author = message.getAuthor();

            Element authorElement = document.createElement("div");
            authorElement.addClass("chatlog__author-avatar-container");

            Element authorAvatar = document.createElement("img");
            authorAvatar.addClass("chatlog__author-avatar");
            authorAvatar.attr("src", author.getAvatarUrl());
            authorAvatar.attr("alt", "Avatar");
            authorAvatar.attr("loading", "lazy");

            authorElement.appendChild(authorAvatar);
            messageGroup.appendChild(authorElement);

            // message content
            Element content = document.createElement("div");
            content.addClass("chatlog__messages");
            // message author name
            Element authorName = document.createElement("span");
            authorName.addClass("chatlog__author-name");
            // authorName.attr("title", author.getName()); // author.name
            authorName.attr("title", author.getAsTag());
            authorName.text(author.getName());
            authorName.attr("data-user-id", author.getId());
            content.appendChild(authorName);

            if (author.isBot()) {
                Element botTag = document.createElement("span");
                botTag.addClass("chatlog__bot-tag").text("BOT");
                content.appendChild(botTag);
            }

            // timestamp
            Element timestamp = document.createElement("span");
            timestamp.addClass("chatlog__timestamp");
            timestamp
                    .text(message.getTimeCreated().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

            content.appendChild(timestamp);

            Element messageContent = document.createElement("div");
            messageContent.addClass("chatlog__message");
            messageContent.attr("data-message-id", message.getId());
            messageContent.attr("id", "message-" + message.getId());
            messageContent.attr("title", "Message sent: "
                    + message.getTimeCreated().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

            if (message.getContentDisplay().length() > 0) {
                Element messageContentContent = document.createElement("div");
                messageContentContent.addClass("chatlog__content");

                Element messageContentContentMarkdown = document.createElement("div");
                messageContentContentMarkdown.addClass("markdown");

                Element messageContentContentMarkdownSpan = document.createElement("span");
                messageContentContentMarkdownSpan.addClass("preserve-whitespace");
//                System.out.println(message.getContentDisplay());
//                System.out.println(message.getContentDisplay().length());
//                System.out.println(message.getContentStripped());
//                System.out.println(message.getContentRaw());
//                System.out.println(message.getContentDisplay().contains("\n"));
//                System.out.println(message.getContentDisplay().contains("\r"));
//                System.out.println(message.getContentDisplay().contains("\r\n"));
                messageContentContentMarkdownSpan
                        .html(Formatter.format(message.getContentDisplay()));

                messageContentContentMarkdown.appendChild(messageContentContentMarkdownSpan);
                messageContentContent.appendChild(messageContentContentMarkdown);
                messageContent.appendChild(messageContentContent);
            }

            // messsage attachments
            if (!message.getAttachments().isEmpty()) {
                for (Message.Attachment attach : message.getAttachments()) {
                    Element attachmentsDiv = document.createElement("div");
                    attachmentsDiv.addClass("chatlog__attachment");

                    String attachmentType = attach.getFileExtension();
                    if (imageFormats.contains(attachmentType)) {
                        //          System.out.println("UNGA IMAGEN WEBON XD");
                        Element attachmentLink = document.createElement("a");

                        Element attachmentImage = document.createElement("img");
                        attachmentImage.addClass("chatlog__attachment-media");
                        attachmentImage.attr("src", attach.getUrl());
                        attachmentImage.attr("alt", "Image attachment");
                        attachmentImage.attr("loading", "lazy");
                        attachmentImage.attr("title",
                                "Image: " + attach.getFileName() + Formatter.formatBytes(attach.getSize()));

                        attachmentLink.appendChild(attachmentImage);
                        attachmentsDiv.appendChild(attachmentLink);
                    } else if (videoFormats.contains(attachmentType)) {
                        Element attachmentVideo = document.createElement("video");
                        attachmentVideo.addClass("chatlog__attachment-media");
                        attachmentVideo.attr("src", attach.getUrl());
                        attachmentVideo.attr("alt", "Video attachment");
                        attachmentVideo.attr("controls", true);
                        attachmentVideo.attr("title",
                                "Video: " + attach.getFileName() + Formatter.formatBytes(attach.getSize()));

                        attachmentsDiv.appendChild(attachmentVideo);
                    } else if (audioFormats.contains(attachmentType)) {
                        Element attachmentAudio = document.createElement("audio");
                        attachmentAudio.addClass("chatlog__attachment-media");
                        attachmentAudio.attr("src", attach.getUrl());
                        attachmentAudio.attr("alt", "Audio attachment");
                        attachmentAudio.attr("controls", true);
                        attachmentAudio.attr("title",
                                "Audio: " + attach.getFileName() + Formatter.formatBytes(attach.getSize()));

                        attachmentsDiv.appendChild(attachmentAudio);
                    } else {
                        Element attachmentGeneric = document.createElement("div");
                        attachmentGeneric.addClass("chatlog__attachment-generic");

                        Element attachmentGenericIcon = document.createElement("svg");
                        attachmentGenericIcon.addClass("chatlog__attachment-generic-icon");

                        Element attachmentGenericIconUse = document.createElement("use");
                        attachmentGenericIconUse.attr("xlink:href", "#icon-attachment");

                        attachmentGenericIcon.appendChild(attachmentGenericIconUse);
                        attachmentGeneric.appendChild(attachmentGenericIcon);

                        Element attachmentGenericName = document.createElement("div");
                        attachmentGenericName.addClass("chatlog__attachment-generic-name");

                        Element attachmentGenericNameLink = document.createElement("a");
                        attachmentGenericNameLink.attr("href", attach.getUrl());
                        attachmentGenericNameLink.text(attach.getFileName());

                        attachmentGenericName.appendChild(attachmentGenericNameLink);
                        attachmentGeneric.appendChild(attachmentGenericName);

                        Element attachmentGenericSize = document.createElement("div");
                        attachmentGenericSize.addClass("chatlog__attachment-generic-size");

                        attachmentGenericSize.text(Formatter.formatBytes(attach.getSize()));
                        attachmentGeneric.appendChild(attachmentGenericSize);

                        attachmentsDiv.appendChild(attachmentGeneric);
                    }

                    messageContent.appendChild(attachmentsDiv);
                }
            }

            content.appendChild(messageContent);

            if (!message.getEmbeds().isEmpty()) {
                for (MessageEmbed embed : message.getEmbeds()) {
                    if (embed == null) {
                        continue;
                    }
                    Element embedDiv = document.createElement("div");
                    embedDiv.addClass("chatlog__embed");

                    // embed color
                    if (embed.getColor() != null) {
                        Element embedColorPill = document.createElement("div");
                        embedColorPill.addClass("chatlog__embed-color-pill");
                        embedColorPill.attr("style",
                                "background-color: #" + Formatter.toHex(embed.getColor()));

                        embedDiv.appendChild(embedColorPill);
                    }

                    Element embedContentContainer = document.createElement("div");
                    embedContentContainer.addClass("chatlog__embed-content-container");

                    Element embedContent = document.createElement("div");
                    embedContent.addClass("chatlog__embed-content");

                    Element embedText = document.createElement("div");
                    embedText.addClass("chatlog__embed-text");

                    // embed author
                    if (embed.getAuthor() != null && embed.getAuthor().getName() != null) {
                        Element embedAuthor = document.createElement("div");
                        embedAuthor.addClass("chatlog__embed-author");

                        if (embed.getAuthor().getIconUrl() != null) {
                            Element embedAuthorIcon = document.createElement("img");
                            embedAuthorIcon.addClass("chatlog__embed-author-icon");
                            embedAuthorIcon.attr("src", embed.getAuthor().getIconUrl());
                            embedAuthorIcon.attr("alt", "Author icon");
                            embedAuthorIcon.attr("loading", "lazy");

                            embedAuthor.appendChild(embedAuthorIcon);
                        }

                        Element embedAuthorName = document.createElement("span");
                        embedAuthorName.addClass("chatlog__embed-author-name");

                        if (embed.getAuthor().getUrl() != null) {
                            Element embedAuthorNameLink = document.createElement("a");
                            embedAuthorNameLink.addClass("chatlog__embed-author-name-link");
                            embedAuthorNameLink.attr("href", embed.getAuthor().getUrl());
                            embedAuthorNameLink.text(embed.getAuthor().getName());

                            embedAuthorName.appendChild(embedAuthorNameLink);
                        } else {
                            embedAuthorName.text(embed.getAuthor().getName());
                        }

                        embedAuthor.appendChild(embedAuthorName);
                        embedText.appendChild(embedAuthor);
                    }

                    // embed title
                    if (embed.getTitle() != null) {
                        Element embedTitle = document.createElement("div");
                        embedTitle.addClass("chatlog__embed-title");

                        if (embed.getUrl() != null) {
                            Element embedTitleLink = document.createElement("a");
                            embedTitleLink.addClass("chatlog__embed-title-link");
                            embedTitleLink.attr("href", embed.getUrl());

                            Element embedTitleMarkdown = document.createElement("div");
                            embedTitleMarkdown.addClass("markdown preserve-whitespace")
                                    .html(Formatter.format(embed.getTitle()));

                            embedTitleLink.appendChild(embedTitleMarkdown);
                            embedTitle.appendChild(embedTitleLink);
                        } else {
                            Element embedTitleMarkdown = document.createElement("div");
                            embedTitleMarkdown.addClass("markdown preserve-whitespace")
                                    .html(Formatter.format(embed.getTitle()));

                            embedTitle.appendChild(embedTitleMarkdown);
                        }
                        embedText.appendChild(embedTitle);
                    }

                    // embed description
                    if (embed.getDescription() != null) {
                        Element embedDescription = document.createElement("div");
                        embedDescription.addClass("chatlog__embed-description");

                        Element embedDescriptionMarkdown = document.createElement("div");
                        embedDescriptionMarkdown.addClass("markdown preserve-whitespace");
                        embedDescriptionMarkdown
                                .html(Formatter.format(embed.getDescription()));

                        embedDescription.appendChild(embedDescriptionMarkdown);
                        embedText.appendChild(embedDescription);
                    }

                    // embed fields
                    if (!embed.getFields().isEmpty()) {
                        Element embedFields = document.createElement("div");
                        embedFields.addClass("chatlog__embed-fields");

                        for (MessageEmbed.Field field : embed.getFields()) {
                            Element embedField = document.createElement("div");
                            embedField.addClass(field.isInline() ? "chatlog__embed-field-inline"
                                    : "chatlog__embed-field");

                            // Field nmae
                            Element embedFieldName = document.createElement("div");
                            embedFieldName.addClass("chatlog__embed-field-name");

                            Element embedFieldNameMarkdown = document.createElement("div");
                            embedFieldNameMarkdown.addClass("markdown preserve-whitespace");
                            embedFieldNameMarkdown.html(field.getName());

                            embedFieldName.appendChild(embedFieldNameMarkdown);
                            embedField.appendChild(embedFieldName);


                            // Field value
                            Element embedFieldValue = document.createElement("div");
                            embedFieldValue.addClass("chatlog__embed-field-value");

                            Element embedFieldValueMarkdown = document.createElement("div");
                            embedFieldValueMarkdown.addClass("markdown preserve-whitespace");
                            embedFieldValueMarkdown
                                    .html(Formatter.format(field.getValue()));

                            embedFieldValue.appendChild(embedFieldValueMarkdown);
                            embedField.appendChild(embedFieldValue);

                            embedFields.appendChild(embedField);
                        }

                        embedText.appendChild(embedFields);
                    }

                    embedContent.appendChild(embedText);

                    // embed thumbnail
                    if (embed.getThumbnail() != null) {
                        Element embedThumbnail = document.createElement("div");
                        embedThumbnail.addClass("chatlog__embed-thumbnail-container");

                        Element embedThumbnailLink = document.createElement("a");
                        embedThumbnailLink.addClass("chatlog__embed-thumbnail-link");
                        embedThumbnailLink.attr("href", embed.getThumbnail().getUrl());

                        Element embedThumbnailImage = document.createElement("img");
                        embedThumbnailImage.addClass("chatlog__embed-thumbnail");
                        embedThumbnailImage.attr("src", embed.getThumbnail().getUrl());
                        embedThumbnailImage.attr("alt", "Thumbnail");
                        embedThumbnailImage.attr("loading", "lazy");

                        embedThumbnailLink.appendChild(embedThumbnailImage);
                        embedThumbnail.appendChild(embedThumbnailLink);

                        embedContent.appendChild(embedThumbnail);
                    }

                    embedContentContainer.appendChild(embedContent);

                    // embed image
                    if (embed.getImage() != null) {
                        Element embedImage = document.createElement("div");
                        embedImage.addClass("chatlog__embed-image-container");

                        Element embedImageLink = document.createElement("a");
                        embedImageLink.addClass("chatlog__embed-image-link");
                        embedImageLink.attr("href", embed.getImage().getUrl());

                        Element embedImageImage = document.createElement("img");
                        embedImageImage.addClass("chatlog__embed-image");
                        embedImageImage.attr("src", embed.getImage().getUrl());
                        embedImageImage.attr("alt", "Image");
                        embedImageImage.attr("loading", "lazy");

                        embedImageLink.appendChild(embedImageImage);
                        embedImage.appendChild(embedImageLink);

                        embedContentContainer.appendChild(embedImage);
                    }

                    // embed footer
                    if (embed.getFooter() != null) {
                        Element embedFooter = document.createElement("div");
                        embedFooter.addClass("chatlog__embed-footer");

                        if (embed.getFooter().getIconUrl() != null) {
                            Element embedFooterIcon = document.createElement("img");
                            embedFooterIcon.addClass("chatlog__embed-footer-icon");
                            embedFooterIcon.attr("src", embed.getFooter().getIconUrl());
                            embedFooterIcon.attr("alt", "Footer icon");
                            embedFooterIcon.attr("loading", "lazy");

                            embedFooter.appendChild(embedFooterIcon);
                        }

                        Element embedFooterText = document.createElement("span");
                        embedFooterText.addClass("chatlog__embed-footer-text");
                        embedFooterText.text(embed.getTimestamp() != null
                                ? embed.getFooter().getText() + " • " + embed.getTimestamp()
                                .format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                                : embed.getFooter().getText());

                        embedFooter.appendChild(embedFooterText);

                        embedContentContainer.appendChild(embedFooter);
                    }

                    embedDiv.appendChild(embedContentContainer);
                    content.appendChild(embedDiv);
                }
            }

            messageGroup.appendChild(content);
            chatLog.appendChild(messageGroup);
        }
        return new ByteArrayInputStream(document.outerHtml().getBytes());
    }

    private File findFile(String fileName) {
        URL url = getClass().getClassLoader().getResource(fileName);
        if (url == null) {
            throw new IllegalArgumentException("file is not found: " + fileName);
        }
        return new File(url.getFile());
    }
}