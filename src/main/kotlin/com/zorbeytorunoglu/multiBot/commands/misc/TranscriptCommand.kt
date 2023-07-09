package com.zorbeytorunoglu.multiBot.commands.misc

import com.zorbeytorunoglu.multiBot.Bot
import com.zorbeytorunoglu.multiBot.commands.Command
import com.zorbeytorunoglu.multiBot.permissions.Permission
import com.zorbeytorunoglu.multiBot.transcript.Transcript
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.utils.FileUpload
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class TranscriptCommand(private val bot: Bot): Command {
    override val name: String
        get() = bot.commandsConfigurationHandler.commands.transcriptCmd
    override val description: String
        get() = bot.commandsConfigurationHandler.commands.transcriptDesc
    override val guildOnly: Boolean
        get() = true

    override fun optionData(): Collection<OptionData> {

        return listOf(
            OptionData(
                OptionType.INTEGER, "messages", "Messages to create a transcript of.", false, false
            )
        )

    }

    override fun subcommandData(): Collection<SubcommandData> {
        return emptyList()
    }

    override fun execute(event: SlashCommandInteractionEvent) {

        if (!bot.permissionManager.hasPermission(event.member!!, Permission.TRANSCRIPT)) {
            event.reply(bot.messagesHandler.messages.noPermission)
                .setEphemeral(true).queue()
            return
        }

        if (event.getOption("messages") != null) {
            val messageCount = event.getOption("messages")!!.asInt

            val msg: InteractionHook = event.reply(bot.messagesHandler.messages.transcriptRetrievingMessages).complete()

            event.channel.asTextChannel().history.retrievePast(messageCount).queue {

                msg.editOriginal(bot.messagesHandler.messages.transcriptMessagesRetrieved).queue()

                val html = createFileFromInputStream(Transcript().generateFromMessages(it), event.channel.name)

                msg.deleteOriginal().queue()

                event.channel.sendMessage(bot.messagesHandler.messages.transcriptGenerated)
                    .addFiles(FileUpload.fromData(html)).queue()

            }

        } else {

            val msg = event.reply(bot.messagesHandler.messages.transcriptGenerating).complete()

            Transcript().createTranscript(event.messageChannel, event.channel.asTextChannel(), "${event.channel.asTextChannel().name}.html")

            msg.editOriginal("Transcript is generated!").queue()

        }

    }

    private fun createFileFromInputStream(inputStream: InputStream, fileName: String): File {
        val outputFile = File("$fileName.html") // Generates a unique file name
        val buffer = ByteArray(8192) // 8 KB buffer size (can be adjusted as needed)

        val outputStream = FileOutputStream(outputFile)

        try {
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()
        } finally {
            try {
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            try {
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return outputFile
    }


}