package com.zorbeytorunoglu.multiBot.task

import com.zorbeytorunoglu.multiBot.Bot
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel

class TaskManager(private val bot: Bot) {

    fun isTicket(threadChannel: ThreadChannel): Boolean {

        //TODO: Will be changed

        return threadChannel.ownerId == bot.jda.selfUser.id

    }

    fun getChannel(id: String): ThreadChannel? {

        return bot.jda.getGuildChannelById(id)?.guild?.getThreadChannelById(id)

    }

}