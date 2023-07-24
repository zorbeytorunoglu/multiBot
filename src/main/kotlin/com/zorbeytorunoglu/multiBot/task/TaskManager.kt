package com.zorbeytorunoglu.multiBot.task

import com.zorbeytorunoglu.multiBot.Bot
import kotlinx.coroutines.*
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

    init {

        println("${loadTaskChannels()} task channels are loaded.")

        CoroutineScope(Dispatchers.IO).launch {

            println("${loadTasks()} tasks are loaded.")

        }

    }

    fun loadTaskChannels(): Int {

        if (bot.jda.guilds.isEmpty()) return 0

        val preSetName = bot.settingsHandler.settings.tasksForumChannelName

        var loadedChannels = 0

        for (guild in bot.jda.guilds) {

            if (guild.forumChannels.isEmpty()) continue

            for (forumChannel in guild.forumChannels) {

                if (forumChannel.name != preSetName) continue

                if (forumChannel.topic == null) continue

                if (!forumChannel.topic!!.startsWith("taskChannel")) continue

                val topic = forumChannel.topic!!.split(":")

                if (topic.size != 3) continue

                val roles = topic[1].split(",").filter { forumChannel.guild.getRoleById(it) != null }

                val headRoles = topic[2].split(",").filter { forumChannel.guild.getRoleById(it) != null }

                if (roles.isNotEmpty() && headRoles.isNotEmpty()) {
                    taskChannels.add(TaskChannel(forumChannel.id, roles, headRoles))
                    loadedChannels++
                }

            }

        }

        return loadedChannels

    }

//    fun loadTasks() {
//
//        if (taskChannels.isEmpty()) return
//
//        for (taskChannel in taskChannels) {
//
//            val forumChannel = bot.jda.getForumChannelById(taskChannel.channelId) ?: continue
//
//            if (forumChannel.threadChannels.isEmpty()) continue
//
//            for (threadChannel in forumChannel.threadChannels) {
//
//                threadChannel.retrieveStartMessage().queue {
//
//                    if (it.embeds.isNotEmpty()) {
//                        val taskData = getTaskDataFromEmbed(threadChannel, it.embeds[0])
//                        if (taskData != null) taskChannel.tasks.add(Task(bot, taskData))
//                    }
//
//                }
//
//            }
//
//        }
//
//    }

    suspend fun loadTasks(): Int = coroutineScope {
        if (taskChannels.isEmpty()) return@coroutineScope 0

        val tasksDeferred = mutableListOf<Deferred<Int>>()

        for (taskChannel in taskChannels) {
            val forumChannel = bot.jda.getForumChannelById(taskChannel.channelId) ?: continue

            if (forumChannel.threadChannels.isEmpty()) continue

            for (threadChannel in forumChannel.threadChannels) {
                val deferredTask = async {
                    val startMessage = threadChannel.retrieveStartMessage().complete()
                    if (startMessage.embeds.isNotEmpty()) {
                        val taskData = getTaskDataFromEmbed(threadChannel, startMessage.embeds[0])
                        if (taskData != null) {
                            taskChannel.tasks.add(Task(bot, taskData))
                            1 // Return 1 for each task added
                        } else {
                            0 // Return 0 if taskData is null
                        }
                    } else {
                        0 // Return 0 if there are no embeds
                    }
                }
                tasksDeferred.add(deferredTask)
            }
        }

        tasksDeferred.sumOf { it.await() }

    }

    fun getTaskDataFromEmbed(threadChannel: ThreadChannel, messageEmbed: MessageEmbed): TaskData? {

        if (messageEmbed.fields.isEmpty()) return null

        var deadline: String? = null
        var assignees: Collection<String>? = null
        var watchers: Collection<String>? = null
        var givenBy: String? = null
        val taskId: String = threadChannel.id
        var status: String? = null
        var priority: String? = null

        for (field in messageEmbed.fields) {
            when (field.name) {
                bot.messagesHandler.messages.taskEmbedDeadline -> deadline = field.value
                bot.messagesHandler.messages.taskEmbedGivenBy -> givenBy = field.value!!.drop(2).dropLast(1)
                bot.messagesHandler.messages.taskEmbedAssignees -> {
                    assignees = field.value!!.split(",").map {
                        it.trim().drop(2).dropLast(1)
                    }
                }
                bot.messagesHandler.messages.taskEmbedWatchers -> {
                    watchers = field.value!!.split(",").map {
                        it.trim().drop(2).dropLast(1)
                    }
                }
                bot.messagesHandler.messages.taskEmbedStatus -> status = field.value!!.replace(" ", "_")
                bot.messagesHandler.messages.taskEmbedPriority -> priority = field.value!!.replace(" ", "_")
            }
        }

        return if (givenBy == null || status == null || priority == null)
            null
        else TaskData(taskId,givenBy, assignees, watchers, deadline, status, priority)

    }

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

    fun getTaskChannel(threadChannel: ThreadChannel): TaskChannel? {
        return taskChannels.find { taskChannel ->
            taskChannel.channelId == threadChannel.parentChannel.id
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
            builder.addField(MessageEmbed.Field(bot.messagesHandler.messages.taskEmbedDeadline,
                bot.taskManager.taskDateFormat.format(deadline), true))
        }

        val assignees = task.getAssignees()
        if (assignees.isNotEmpty()) {
            val assigneesString = assignees.joinToString(",") { it.asMention }
            builder.addField(MessageEmbed.Field(
                bot.messagesHandler.messages.taskEmbedAssignees, assigneesString, true))
        }

        val watchers = task.getWatchers()
        if (watchers.isNotEmpty()) {
            val watchersString = watchers.joinToString(",") { it.asMention }
            builder.addField(MessageEmbed.Field(
                bot.messagesHandler.messages.taskEmbedWatchers, watchersString, true))
        }

        val priority = task.priority.toString().replace("_", "")
        builder.addField(MessageEmbed.Field(
            bot.messagesHandler.messages.taskEmbedPriority, priority, true))

        val status = task.status.toString().replace("_", "")
        builder.addField(MessageEmbed.Field(
            bot.messagesHandler.messages.taskEmbedStatus, status, true))

        val taskGiver = task.getTaskGiver()
        if (taskGiver != null) {
            builder.addField(MessageEmbed.Field(
                bot.messagesHandler.messages.taskEmbedGivenBy, taskGiver.asMention, true))
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