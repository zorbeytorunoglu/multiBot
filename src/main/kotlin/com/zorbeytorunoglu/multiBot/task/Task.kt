package com.zorbeytorunoglu.multiBot.task

import com.zorbeytorunoglu.multiBot.Bot
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import java.text.SimpleDateFormat
import java.util.*

class Task(private val bot: Bot, val taskData: TaskData) {

    val deadline: Date
    val status: TaskStatus
    val priority: TaskPriority

    init {

        val dateFormat = SimpleDateFormat(bot.settingsHandler.settings.taskDateFormat)
        deadline = dateFormat.parse(taskData.deadline)

        status = TaskStatus.valueOf(taskData.status)

        priority = TaskPriority.valueOf(taskData.priority)

    }

    fun getChannel(): ThreadChannel? {

        return bot.jda.getGuildChannelById(taskData.taskId)?.guild?.getThreadChannelById(taskData.taskId)

    }

    fun getGuild(): Guild? {

        return bot.jda.getGuildChannelById(taskData.taskId)?.guild

    }

    fun getWatchers(): List<Member> {

        return validateMembers(taskData.watchers)

    }

    fun getAssignees(): List<Member> {

        return validateMembers(taskData.assignees)

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