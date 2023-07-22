package com.zorbeytorunoglu.multiBot.task

import com.zorbeytorunoglu.multiBot.Bot
import kotlinx.coroutines.CompletableDeferred
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.concrete.Category
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.forums.ForumTag
import net.dv8tion.jda.api.entities.channel.forums.ForumTagData
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion
import net.dv8tion.jda.api.entities.emoji.Emoji
import java.awt.Color
import java.text.SimpleDateFormat

class TaskManager(private val bot: Bot) {

    val taskChannels = ArrayList<TaskChannel>()
    val taskDateFormat = SimpleDateFormat(bot.settingsHandler.settings.taskDateFormat)

    val defaultTagsData = mutableListOf(

        ForumTagData("in-progress")
            .setName(bot.settingsHandler.settings.inProgressTag)
            .setEmoji(Emoji.fromFormatted(bot.settingsHandler.settings.inProgressTagEmoji))
            .setModerated(true),
        ForumTagData("open")
            .setName(bot.settingsHandler.settings.openTag)
            .setEmoji(Emoji.fromFormatted(bot.settingsHandler.settings.openTagEmoji))
            .setModerated(true),
        ForumTagData("done")
            .setName(bot.settingsHandler.settings.doneTag)
            .setEmoji(Emoji.fromFormatted(bot.settingsHandler.settings.doneTagEmoji))
            .setModerated(true)

    )

    fun isTask(threadChannel: ThreadChannel): Boolean {

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

    fun refreshTags(bot: Bot, guild: Guild, taskChannel: TaskChannel): CompletableDeferred<Unit> {

        val deferred = CompletableDeferred<Unit>()

        val tags: MutableList<ForumTagData> = mutableListOf()

        tags.addAll(defaultTagsData)

        guild.getMembersWithRoles(taskChannel.getRoles(guild)).forEach {

            val name = if (bot.settingsHandler.settings.nicknamesInTags && it.nickname != null)
                it.nickname!! else it.effectiveName

            tags.add(ForumTagData(it.id).setName(name))

        }

        taskChannel.getChannel(guild)!!.manager.setAvailableTags(tags).queue {
            deferred.complete(Unit)
        }

        return deferred

    }

    fun generateTaskStartMessage(taskData: TaskData): String {
        val builder = StringBuilder()

        val assignees = taskData.assignees?.joinToString(",") ?: "null"
        val departmentRoles = taskData.departmentRoles.joinToString(",")
        val watchers = taskData.watchers?.joinToString(",") ?: "null"

        with(builder) {
            append("||")
            append("${taskData.taskId}:${taskData.givenBy}:${taskData.priority}:${taskData.status}:$assignees:")
            append("${taskData.deadline ?: "null"}:")
            append("$departmentRoles:")
            append(watchers)
            append("||")
        }

        return builder.toString()
    }

    fun taskDataFromStartMessage(startMessage: String): TaskData {

        val args = startMessage.drop(2).dropLast(2).split(":")

        val id = args[0]
        val givenBy = args[1]
        val priority = args[2]
        val status = args[3]
        val assignees = if (args[4] == "null") null else args[3].split(",").toList()
        val deadline = if (args[5] == "null") null else args[4]
        val departmentRoles = args[6].split(",").toList()
        val watchers = if (args[7] == "null") null else args[6].split(",").toList()

        return TaskData(id,givenBy,assignees,watchers,deadline,status,priority,departmentRoles)

    }

    fun isValidStartMessage(string: String): Boolean {
        return string.startsWith("||") && string.endsWith("||") && string.count { it == ':' } == 7
    }

    fun getAssigneesAsTags(nicknameEnabled: Boolean, forumChannel: ForumChannel, assignees: List<Member>): MutableList<ForumTagSnowflake> {
        val list = mutableListOf<ForumTagSnowflake>()

        val availableTags = forumChannel.availableTags

        for (member in assignees) {
            val name = if (nicknameEnabled && member.nickname != null) member.nickname else member.effectiveName

            val matchingTag = availableTags.find { it.name == name }
            matchingTag?.let { list.add(ForumTagSnowflake.fromId(it.id)) }
        }

        return list
    }

    fun generateTaskEmbed(title: String, description: String, task: Task): EmbedBuilder {
        val builder = EmbedBuilder()
            .setTitle(title)
            .setDescription(description)
            .setColor(Color.BLUE)

        val deadline = task.deadline
        if (deadline != null) {
            builder.addField(MessageEmbed.Field("Deadline", bot.taskManager.taskDateFormat.format(deadline), true))
        }

        val assignees = task.getAssignees()
        if (assignees.isNotEmpty()) {
            val assigneesString = assignees.joinToString(" ") { it.asMention }
            builder.addField(MessageEmbed.Field("Assignees", assigneesString, true))
        }

        val watchers = task.getWatchers()
        if (watchers.isNotEmpty()) {
            val watchersString = watchers.joinToString(" ") { it.asMention }
            builder.addField(MessageEmbed.Field("Watchers", watchersString, true))
        }

        val priority = task.priority.toString().replace("_", "")
        builder.addField(MessageEmbed.Field("Priority", priority, true))

        val status = task.status.toString().replace("_", "")
        builder.addField(MessageEmbed.Field("Status", status, true))

        val taskGiver = task.getTaskGiver()
        if (taskGiver != null) {
            builder.addField(MessageEmbed.Field("Given By", taskGiver.asMention, true))
        }

        return builder
    }

    fun getTag(forumChannel: ForumChannel, status: TaskStatus): ForumTag? {

        return when (status) {
            TaskStatus.OPEN -> forumChannel.availableTags.firstOrNull { it.name == bot.settingsHandler.settings.openTag }
            TaskStatus.IN_PROGRESS -> forumChannel.availableTags.firstOrNull { it.name == bot.settingsHandler.settings.inProgressTag }
            TaskStatus.DONE -> forumChannel.availableTags.firstOrNull { it.name == bot.settingsHandler.settings.doneTag }
        }

    }

}