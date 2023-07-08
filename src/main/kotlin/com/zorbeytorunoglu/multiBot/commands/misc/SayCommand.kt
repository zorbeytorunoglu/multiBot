package com.zorbeytorunoglu.multiBot.commands.misc

import com.zorbeytorunoglu.multiBot.Bot
import com.zorbeytorunoglu.multiBot.commands.Command
import com.zorbeytorunoglu.multiBot.permissions.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import org.slf4j.LoggerFactory

class SayCommand(private val bot: Bot): Command {

    //TODO: Should implement the logger to every class
    private val logger = LoggerFactory.getLogger(SayCommand::class.java)

    override val name: String
        get() = bot.commandsConfigurationHandler.commands.sayCmd
    override val description: String
        get() = bot.commandsConfigurationHandler.commands.sayDesc
    override val guildOnly: Boolean
        get() = false

    override fun optionData(): Collection<OptionData> {
        return listOf(
            OptionData(
                OptionType.STRING, "message", "Message to be sent.", true, false
            )
        )
    }

    override fun subcommandData(): Collection<SubcommandData> {
        return emptyList()
    }

    override fun execute(event: SlashCommandInteractionEvent) {

        logger.debug("SayCommand execution is triggered by ${event.user.id}")

        if (!bot.permissionManager.hasPermission(event.user.id, event, Permission.SAY)) {
            event.reply(bot.messagesHandler.messages.noPermission)
                .setEphemeral(true).queue()
            logger.debug("${event.user.id} has no permission to use SayCommand")
            return
        }

        event.channel.sendMessage(event.getOption("message")!!.asString).queue()

        event.reply(bot.messagesHandler.messages.saySent)
            .setEphemeral(true).queue()

        logger.debug("SayCommand is executed successfully by ${event.user.id}. Message: ${event.getOption("message")!!.asString}")

    }


}