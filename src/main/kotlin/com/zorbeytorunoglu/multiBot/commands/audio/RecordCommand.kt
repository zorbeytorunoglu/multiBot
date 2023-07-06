package com.zorbeytorunoglu.multiBot.commands.audio

import com.zorbeytorunoglu.multiBot.Bot
import com.zorbeytorunoglu.multiBot.audio.RecordingAudioHandler
import com.zorbeytorunoglu.multiBot.commands.Command
import com.zorbeytorunoglu.multiBot.permissions.Permission
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

class RecordCommand(private val bot: Bot): Command {

    override val name: String
        get() = bot.commandsConfigurationHandler.commands.recordCmd
    override val description: String
        get() = bot.commandsConfigurationHandler.commands.recordDesc
    override val guildOnly: Boolean
        get() = true

    override fun optionData(): Collection<OptionData> {
        return emptyList()
    }

    override fun subcommandData(): Collection<SubcommandData> {
        return listOf(
            SubcommandData("start", "Starts recording.")
                .addOptions(OptionData(OptionType.CHANNEL,"channel","Voice channel.",
                    false, false)),
            SubcommandData("stop", "Stops recording.")
        )
    }

    override fun execute(event: SlashCommandInteractionEvent) {

        if (!bot.permissionManager.hasPermission(event.member!!, Permission.RECORD)) {
            event.reply(bot.messagesHandler.messages.noPermission)
                .setEphemeral(true).queue()
            return
        }

        if (event.subcommandName == "start") {

            var channel: GuildChannelUnion

            if (event.getOption("channel") != null) {
                channel = event.getOption("channel")!!.asChannel
            } else {
                if (event.member!!.voiceState == null) {
                    event.reply(bot.messagesHandler.messages.notInVoiceChannel)
                        .setEphemeral(true).queue()
                    return
                } else {
                    if (event.member!!.voiceState!!.channel == null) {
                        event.reply(bot.messagesHandler.messages.notInVoiceChannel)
                            .setEphemeral(true).queue()
                        return
                    } else {
                        channel = event.member!!.voiceState!!.channel!! as GuildChannelUnion
                    }
                }
            }

            if (channel !is AudioChannel) {
                event.reply(bot.messagesHandler.messages.notAudioChannel)
                    .setEphemeral(true).queue()
                return
            }

            val audioManager = event.guild!!.audioManager

            if (audioManager.isConnected) {
                event.reply(bot.messagesHandler.messages.alreadyConnected)
                    .setEphemeral(true).queue()
                return
            }

            val handler = RecordingAudioHandler()

            audioManager.receivingHandler = handler
            audioManager.sendingHandler = handler

            audioManager.openAudioConnection(channel.asAudioChannel())

            event.reply(bot.messagesHandler.messages.recordingStarted).setEphemeral(true)
                .queue()

        } else if (event.subcommandName == "stop") {

            if (!event.guild!!.audioManager.isConnected) {
                event.reply(bot.messagesHandler.messages.notRecording)
                    .setEphemeral(true).queue()
                return
            }

            event.guild!!.audioManager.closeAudioConnection()

            event.reply(bot.messagesHandler.messages.recordingStopped)
                .setEphemeral(true).queue()

        }

//        val audioChannel = event.getOption("channel")!!.asChannel
//
//        val guild = event.guild!!
//        val messageChannel = event.channel
//        val audioManager = guild.audioManager
//
//        if (audioManager.isConnected) {
//            println("already connected")
//            return
//        }
//
//        val handler = RecordingAudioHandler()
//
//        audioManager.sendingHandler = handler
//        audioManager.receivingHandler = handler
//
//        audioManager.openAudioConnection(audioChannel.asAudioChannel())

    }


}