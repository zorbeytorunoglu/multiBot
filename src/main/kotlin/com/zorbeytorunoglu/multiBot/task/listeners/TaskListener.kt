package com.zorbeytorunoglu.multiBot.task.listeners

import com.zorbeytorunoglu.multiBot.Bot
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class TaskListener(private val bot: Bot): ListenerAdapter() {

    override fun onChannelDelete(event: ChannelDeleteEvent) {

        if (event.channel.type == ChannelType.FORUM) {
            val taskChannel = bot.taskManager.taskChannels.find { it.channelId == event.channel.id }
            if (taskChannel != null) bot.taskManager.taskChannels.remove(taskChannel)
        }

    }

}