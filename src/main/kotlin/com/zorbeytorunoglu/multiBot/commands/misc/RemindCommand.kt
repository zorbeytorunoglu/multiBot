package com.zorbeytorunoglu.multiBot.commands.misc

import com.zorbeytorunoglu.multiBot.Bot
import com.zorbeytorunoglu.multiBot.commands.Command
import com.zorbeytorunoglu.multiBot.permissions.Permission
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import java.util.concurrent.TimeUnit

class RemindCommand(private val bot: Bot): Command {
    override val name: String
        get() = bot.commandsConfigurationHandler.commands.remindCmd
    override val description: String
        get() = bot.commandsConfigurationHandler.commands.remindDesc
    override val guildOnly: Boolean
        get() = true

    override fun optionData(): Collection<OptionData> {
        return listOf(
            OptionData(OptionType.INTEGER, "time","Time.", true, false),
            OptionData(OptionType.STRING, "timeunit", "s/m/h", false, false),
            OptionData(OptionType.STRING, "note", "Reminder note.", false, false)
        )
    }

    override fun subcommandData(): Collection<SubcommandData> {
        return emptyList()
    }

    override fun execute(event: SlashCommandInteractionEvent) {

        if (!bot.permissionManager.hasPermission(event.member!!, Permission.REMIND)) {
            event.reply(bot.messagesHandler.messages.noPermission).setEphemeral(true).queue()
            return
        }

        val time = event.getOption("time")!!.asLong

        var timeUnit: TimeUnit = TimeUnit.MINUTES

        if (event.getOption("timeunit") != null) {
            timeUnit = when (event.getOption("timeunit")!!.asString) {
                "s" -> TimeUnit.SECONDS
                "m" -> TimeUnit.MINUTES
                "h" -> TimeUnit.HOURS
                else -> TimeUnit.MINUTES
            }
        }

        val builder = EmbedBuilder()

        if (event.getOption("note") != null) {
            builder.setTitle(bot.messagesHandler.messages.reminder)
            builder.setDescription(event.getOption("note")!!.asString)
        } else {
            builder.setDescription(bot.messagesHandler.messages.reminder)
        }

        event.channel.sendMessage(event.member!!.asMention).queueAfter(time, timeUnit)

        event.member?.asMention?.let { msg ->
            event.channel.sendMessage(msg).queueAfter(time, timeUnit) {
            it.delete().queueAfter(1L, TimeUnit.SECONDS) {
                event.channel.sendMessageEmbeds(builder.build()).queue()
            }
        } }

        event.reply(bot.messagesHandler.messages.reminderSet).setEphemeral(true).queue()

    }


}