package com.zorbeytorunoglu.multiBot.ticket.listeners

import com.zorbeytorunoglu.multiBot.Bot
import com.zorbeytorunoglu.multiBot.configuration.embedmessage.EmbedMessage
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class TicketButtonListener(private val bot: Bot): ListenerAdapter() {

    override fun onButtonInteraction(event: ButtonInteractionEvent) {

        if (!event.isFromGuild) return

        val ticketButton =
            bot.ticketHandler.getTicketButton(event.button) ?: return

        if (event.guild!!.getCategoryById(ticketButton.targetCategoryId) == null) {
            event.reply(bot.messagesHandler.messages.targetCategoryNotFound).setEphemeral(true)
            return
        }

        val title = ticketButton.ticketButtonConfig.channelTitleFormat
            .replace("%ticket_id%", "1")
            .replace("%member_id%", event.member!!.id)

        val category = event.guild!!.getCategoryById(ticketButton.targetCategoryId)!!

        category.createTextChannel(title).queue {
            //TODO: Register ticket

            if (ticketButton.ticketButtonConfig.openingEmbed != null) {

                it.sendMessageEmbeds(EmbedMessage(ticketButton.ticketButtonConfig.openingEmbed).embedMessage).queue()

            }

            event.reply(bot.messagesHandler.messages.ticketReady.replace("%ticket%", it.asMention))
                .setEphemeral(true).queue()

        }

        //TODO: Ticket IDs/NOs
        //TODO: Check for previous tickets.

    }

}