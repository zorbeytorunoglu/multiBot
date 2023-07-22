package com.zorbeytorunoglu.multiBot.task

import com.zorbeytorunoglu.multiBot.Bot
import com.zorbeytorunoglu.multiBot.utils.Utils
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import java.util.*

class Task(private val bot: Bot, val taskData: TaskData) {

    val deadline: Date? = if (taskData.deadline == null) null else bot.taskManager.taskDateFormat.parse(taskData.deadline)
    val status: TaskStatus = TaskStatus.valueOf(taskData.status)
    val priority: TaskPriority = TaskPriority.valueOf(taskData.priority)

    fun getChannel(): ThreadChannel? {

        return bot.jda.getGuildChannelById(taskData.taskId)?.guild?.getThreadChannelById(taskData.taskId)

    }

    fun getGuild(): Guild? {

        return bot.jda.getGuildChannelById(taskData.taskId)?.guild

    }

    fun getWatchers(): List<Member> {

        return if (taskData.watchers != null)
            validateMembers(taskData.watchers)
        else emptyList()

    }

    fun getAssignees(): List<Member> {

        return if (taskData.assignees != null)
            validateMembers(taskData.assignees)
        else emptyList()

    }

    fun getTaskGiver(): Member? {
        return getGuild()?.let { Utils.getMember(it, taskData.givenBy) }
    }

    private fun validateMembers(memberIds: Collection<String>): List<Member> {

        if (memberIds.isEmpty()) return emptyList()

        val guild = if (getGuild() == null) return emptyList() else getGuild()!!

        val members = mutableListOf<Member>()

        memberIds.forEach {

            if (guild.getMemberById(it) != null)
                members.add(guild.getMemberById(it)!!)

        }

        return members.toList()

    }

}