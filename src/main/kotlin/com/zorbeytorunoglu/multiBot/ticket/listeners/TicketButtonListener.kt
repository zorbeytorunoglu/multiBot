package com.zorbeytorunoglu.multiBot.ticket.listeners

import com.zorbeytorunoglu.multiBot.Bot
import com.zorbeytorunoglu.multiBot.configuration.embedmessage.EmbedMessage
import com.zorbeytorunoglu.multiBot.configuration.embedmessage.EmbedMessageBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.lang.StringBuilder
import java.util.concurrent.TimeUnit

class TicketButtonListener(private val bot: Bot): ListenerAdapter() {

    override fun onButtonInteraction(event: ButtonInteractionEvent) {

        if (!event.isFromGuild) return

        if (event.button.id == null) return

        val ticketButton =
            bot.ticketHandler.getTicketButton(event.button) ?: return

        if (bot.ticketHandler.exceededTicketLimit(event.member!!, event.button.id!!)) {
            event.reply(bot.messagesHandler.messages.exceedTicketLimit).setEphemeral(true).queue()
            return
        }

        if (event.guild!!.getCategoryById(ticketButton.targetCategoryId) == null) {
            event.reply(bot.messagesHandler.messages.targetCategoryNotFound).setEphemeral(true)
            return
        }

        val title = ticketButton.ticketButtonConfig.channelTitleFormat
            .replace("%member_id%", event.member!!.id)

        val category = event.guild!!.getCategoryById(ticketButton.targetCategoryId)!!

        category.createTextChannel(title).queue {

            //TODO: Adding roles and the member to the ticket may be improved, need to learn more about JDA

            //TODO: Can make permissions configurable

            it.manager.putPermissionOverride(it.guild.publicRole, mutableListOf(), mutableListOf(Permission.VIEW_CHANNEL)).queue {void ->

                it.manager.putMemberPermissionOverride(event.member!!.idLong,
                    mutableListOf(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND,
                        Permission.MESSAGE_HISTORY, Permission.MESSAGE_ATTACH_FILES,
                        Permission.MESSAGE_ADD_REACTION), mutableListOf()
                ).queue { void2 ->
                    if (ticketButton.ticketButtonConfig.rolesToBeAdded != null) {
                        it.manager.setTopic("ticket-"+event.member!!.id+"-"+ticketButton.button.id).queue()
                        val roles = ticketButton.ticketButtonConfig.rolesToBeAdded.split(",")
                        if (roles.isNotEmpty()) {
                            for (role in roles) {

                                if (event.guild!!.getRolesByName(role, true).isEmpty()) continue

                                val roleObj = event.guild!!.getRolesByName(role, true)[0]

                                it.manager.putPermissionOverride(roleObj,
                                    mutableListOf(
                                        Permission.VIEW_CHANNEL,
                                        Permission.MESSAGE_HISTORY,
                                        Permission.MESSAGE_ADD_REACTION,
                                        Permission.MESSAGE_ATTACH_FILES,
                                        Permission.MESSAGE_SEND
                                    ),
                                    mutableListOf()).queue()

                            }
                        }
                    }
                }

            }

            if (ticketButton.ticketButtonConfig.rolesToBePinged != null) {

                val roles = ticketButton.ticketButtonConfig.rolesToBeAdded!!.split(",")
                if (roles.isNotEmpty()) {
                    val sb = StringBuilder()
                    for (role in roles) {
                        if (event.guild!!.getRolesByName(role, true).isEmpty()) continue
                        val roleObj = event.guild!!.getRolesByName(role, true)[0]
                        sb.append(roleObj.asMention).append(",").append(" ")
                    }
                    if (sb.isNotBlank())
                        it.sendMessage(sb.toString()).queue { message ->
                            message.delete().queueAfter(1, TimeUnit.SECONDS)
                        }
                }

            }

            if (ticketButton.ticketButtonConfig.openingEmbed != null) {

                //TODO: Can apply the member placeholder in anywhere in EmbedMessage

                val builder = EmbedMessageBuilder(ticketButton.ticketButtonConfig.openingEmbed).applyPlaceholderAsTag(event.member!!)

                it.sendMessageEmbeds(builder.build()).queue()

            }

            event.reply(bot.messagesHandler.messages.ticketReady.replace("%ticket%", it.asMention))
                .setEphemeral(true).queue()

        }

    }

}