package com.zorbeytorunoglu.multiBot.commands.ticket

import com.zorbeytorunoglu.multiBot.Bot
import com.zorbeytorunoglu.multiBot.commands.Command
import com.zorbeytorunoglu.multiBot.permissions.Permission
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import org.slf4j.LoggerFactory

class AddCommand(private val bot: Bot): Command {

    private val logger = LoggerFactory.getLogger(AddCommand::class.java)

    override val name: String
        get() = bot.commandsConfigurationHandler.commands.addCmd
    override val description: String
        get() = bot.commandsConfigurationHandler.commands.addDesc
    override val guildOnly: Boolean
        get() = true

    override fun optionData(): Collection<OptionData> {
        return listOf(
            OptionData(
                OptionType.USER, "member", "Member to be added to the ticket.", true, false
            )
        )
    }

    override fun subcommandData(): Collection<SubcommandData> {
        return emptyList()
    }

    override fun execute(event: SlashCommandInteractionEvent) {

        logger.debug("Add command is executed by ${event.member!!.id} from ${event.guild!!.id}")

        if (!bot.permissionManager.hasPermission(event.member!!, Permission.TICKET_ADD)) {
            logger.debug("${event.member!!.id} has no permission to use add command.")
            event.reply(bot.messagesHandler.messages.noPermission)
                .setEphemeral(true).queue()
            return
        }

        if (event.channelType != ChannelType.TEXT) {
            logger.debug("Channel type is not text.")
            event.reply(bot.messagesHandler.messages.notATicket)
                .setEphemeral(true).queue()
            return
        }

        if (!bot.ticketHandler.isTicket(event.channel.asTextChannel())) {
            logger.debug("Channel is not a ticket.")
            event.reply(bot.messagesHandler.messages.notATicket)
                .setEphemeral(true).queue()
            return
        }

        val channel = event.channel.asTextChannel()

        //TODO: Member not found, fix cache

        if (event.guild!!.getMemberById(event.getOption("member")!!.asUser.id) == null) {
            logger.debug("User in the option could not be found (null) as member.")
            event.reply(bot.messagesHandler.messages.memberNotFound)
                .setEphemeral(true).queue()
            return
        }

        val member = event.guild!!.getMember(event.getOption("member")!!.asUser)!!

        if (bot.ticketHandler.isInTicket(member, channel)) {
            logger.debug("Mentioned user (${member.id}) is already in the ticket.")
            event.reply(bot.messagesHandler.messages.alreadyInTicket)
                .setEphemeral(true).queue()
            return
        }

        channel.manager.putMemberPermissionOverride(member.idLong,
            mutableListOf(
                net.dv8tion.jda.api.Permission.VIEW_CHANNEL,
                net.dv8tion.jda.api.Permission.MESSAGE_SEND,
                net.dv8tion.jda.api.Permission.MESSAGE_HISTORY,
                net.dv8tion.jda.api.Permission.MESSAGE_ATTACH_FILES,
                net.dv8tion.jda.api.Permission.MESSAGE_ADD_REACTION), mutableListOf()).queue {
                    event.reply(bot.messagesHandler.messages.addedToTicket
                        .replace("%member%", member.asMention)).queue()
        }

        logger.debug("${member.id} is given permissions for the ticket ${channel.id}.")

    }


}