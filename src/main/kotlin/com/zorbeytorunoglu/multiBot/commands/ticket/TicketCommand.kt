package com.zorbeytorunoglu.multiBot.commands.ticket

import com.zorbeytorunoglu.multiBot.Bot
import com.zorbeytorunoglu.multiBot.commands.Command
import com.zorbeytorunoglu.multiBot.permissions.Permission
import com.zorbeytorunoglu.multiBot.transcript.Transcript
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class TicketCommand(private val bot: Bot): Command {

    private val logger = LoggerFactory.getLogger(TicketCommand::class.java)

    private val confirmation = HashMap<String, String>()

    override val name: String
        get() = bot.commandsConfigurationHandler.commands.ticketCmd
    override val description: String
        get() = bot.commandsConfigurationHandler.commands.ticketDesc
    override val guildOnly: Boolean
        get() = true

    override fun optionData(): Collection<OptionData> {
        return emptyList()
    }

    override fun subcommandData(): Collection<SubcommandData> {

        return listOf(
            SubcommandData("add", "Adds a member to the ticket.")
                .addOption(OptionType.USER, "member", "Member to be added to the ticket.", true, false),
            SubcommandData("remove", "Removes someone from the ticket.")
                .addOption(OptionType.USER, "member", "Member to be removed from the ticket.", true, false),
            SubcommandData("delete", "Deletes the ticket.")
                .addOption(OptionType.BOOLEAN, "transcript", "Should print transcript?")
        )

    }

    override fun execute(event: SlashCommandInteractionEvent) {

        if (event.subcommandName == "add") {

            if (!bot.permissionManager.hasPermission(event.member!!, Permission.TICKET_ADD)) {
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

        } else if (event.subcommandName == "remove") {

            if (!bot.permissionManager.hasPermission(event.member!!, Permission.TICKET_REMOVE)) {
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

            if (event.guild!!.getMemberById(event.getOption("member")!!.asUser.id) == null) {
                logger.debug("User in the option could not be found (null) as member.")
                event.reply(bot.messagesHandler.messages.memberNotFound)
                    .setEphemeral(true).queue()
                return
            }

            val member = event.guild!!.getMember(event.getOption("member")!!.asUser)!!

            if (!bot.ticketHandler.isInTicket(member, channel)) {
                logger.debug("Mentioned user (${member.id}) is not in the ticket.")
                event.reply(bot.messagesHandler.messages.notInTicket)
                    .setEphemeral(true).queue()
                return
            }

            channel.manager.putMemberPermissionOverride(member.idLong,
                mutableListOf(),
                mutableListOf(net.dv8tion.jda.api.Permission.VIEW_CHANNEL)).queue {
                event.reply(bot.messagesHandler.messages.addedToTicket
                    .replace("%member%", member.asMention)).queue()
            }

            logger.debug("${member.id} is removed from the ticket ${channel.id}.")

        } else if (event.subcommandName == "delete") {

            if (!bot.permissionManager.hasPermission(event.member!!, Permission.TICKET_DELETE)) {
                event.reply(bot.messagesHandler.messages.noPermission)
                    .setEphemeral(true).queue()
                return
            }

            if (confirmation.containsKey(event.member!!.id)) {

                if (confirmation[event.member!!.id] == event.channel.id) {

                    event.reply(bot.messagesHandler.messages.ticketWillBeDeleted)
                        .queue {

                            if (event.getOption("transcript") != null) {
                                if (event.getOption("transcript")!!.asBoolean) {

                                    Transcript().createTranscript(event.channel)

                                }
                            }

                            event.channel.delete().queueAfter(10, TimeUnit.SECONDS) {
                                confirmation.remove(event.member!!.id)
                            }

                        }

                    return

                }

            }

            event.reply(bot.messagesHandler.messages.deleteConfirm).queue()

            confirmation[event.member!!.id] = event.channel.id

        }

    }


}