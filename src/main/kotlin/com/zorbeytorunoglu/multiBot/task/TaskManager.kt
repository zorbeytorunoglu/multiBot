package com.zorbeytorunoglu.multiBot.task

import com.zorbeytorunoglu.multiBot.Bot
import net.dv8tion.jda.api.entities.channel.concrete.Category
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion

class TaskManager(private val bot: Bot) {

    val taskChannels = ArrayList<TaskChannel>()

    fun isTask(threadChannel: ThreadChannel): Boolean {

        //TODO: Will be changed

        return threadChannel.ownerId == bot.jda.selfUser.id

    }

    fun getTaskChannel(id: String): TaskChannel? {
        if (taskChannels.isEmpty()) return null
        return taskChannels.firstOrNull { it.channelId == id }
    }

    fun getTaskChannel(task: Task): TaskChannel? {
        return taskChannels.find { taskChannel ->
            taskChannel.tasks.any {
                it.taskData.taskId == task.taskData.taskId
            }
        }
    }

    fun getTaskChannel(channel: GuildChannelUnion): TaskChannel? {
        return taskChannels.find { channel.id == it.channelId }
    }

    fun taskForumExists(category: Category): Boolean {

        return category.forumChannels.any { it.name == bot.settingsHandler.settings.tasksForumChannelName }

    }

}