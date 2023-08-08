package com.zorbeytorunoglu.multiBot.task

import com.zorbeytorunoglu.multiBot.Bot
import kotlinx.coroutines.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.Category
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.forums.ForumTag
import net.dv8tion.jda.api.entities.channel.forums.ForumTagData
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake
import net.dv8tion.jda.api.entities.channel.unions.ChannelUnion
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.entities.emoji.Emoji
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class TaskManager(private val bot: Bot) {

    private val logger: Logger = LoggerFactory.getLogger(TaskManager::class.java.name)

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

        if (!bot.settingsHandler.settings.taskSystem) {
            logger.info("Tasks and task channels will not be loaded since it is disabled in settings.json")
        } else {
            println("${loadTaskChannels().size} task channels are loaded.")
            CoroutineScope(Dispatchers.IO).launch {
                println("${loadTasks()} tasks are loaded.")
            }
            if (bot.settingsHandler.settings.scheduledDeadlineCheck)
                startDeadlineCheck()
        }

    }

    /**
     * Loads and initializes TaskChannel objects based on forum channels' topics in guilds.
     *
     * @return A list of loaded TaskChannel objects.
     */
    private fun loadTaskChannels(): List<TaskChannel> {
        /**
         * Check if the bot is a member of any guilds. If not, return an empty list.
         * Initialize a variable to store the predefined task forum channel name.
         * Initialize a list to store loaded TaskChannel objects.
         * Iterate through each guild where the bot is a member.
         * For each guild, iterate through its forum channels and process their topics.
         * Skip forum channels that do not match the predefined name or lack a topic.
         * Parse the topic to extract role and head role information.
         * Create a TaskChannel object with extracted data and add it to taskChannels and loadedChannels if roles and headRoles are present.
         * Return the list of loaded TaskChannel objects.
         */
        if (bot.jda.guilds.isEmpty()) return emptyList()

        val preSetName = bot.settingsHandler.settings.tasksForumChannelName

        val loadedChannels = mutableListOf<TaskChannel>()

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
                    val taskChannel = TaskChannel(forumChannel.id, roles, headRoles)
                    taskChannels.add(taskChannel)
                    loadedChannels.add(taskChannel)
                }
            }
        }

        return loadedChannels
    }

    /**
     * Asynchronously loads tasks from thread channels within associated forum channels.
     *
     * @return The total number of tasks successfully loaded.
     */
    private suspend fun loadTasks(): Int = coroutineScope {
        /**
         * Check if the list of taskChannels is empty. If so, return 0.
         * Initialize a list to store Deferred<Int> objects representing the loading of tasks.
         * Iterate through each taskChannel and its associated forum channels and thread channels.
         * Within each thread channel, asynchronously retrieve the start message and process its embeds.
         * If the start message contains an embed, attempt to extract TaskData from the embed.
         * If TaskData is successfully extracted, create a Task object and add it to the taskChannel's tasks.
         * Return 1 for each task successfully added, otherwise return 0.
         * Sum up the results of all deferred tasks to obtain the total number of tasks loaded.
         */
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

    /**
     * Extracts TaskData from a MessageEmbed within a ThreadChannel.
     *
     * @param threadChannel The ThreadChannel associated with the MessageEmbed.
     * @param messageEmbed The MessageEmbed from which TaskData is being extracted.
     * @return The extracted TaskData object if successfully parsed from the MessageEmbed, or null if parsing fails.
     */
    private fun getTaskDataFromEmbed(threadChannel: ThreadChannel, messageEmbed: MessageEmbed): TaskData? {
        /**
         * Check if the provided messageEmbed contains any fields. If not, return null.
         * Initialize variables to store various task-related data extracted from the messageEmbed.
         * Iterate through the fields of the messageEmbed and extract relevant task data based on field names.
         * Construct a TaskData object using the extracted data.
         * Return the constructed TaskData object if required data (givenBy, status, and priority) are present, otherwise return null.
         */
        if (messageEmbed.fields.isEmpty()) return null

        var deadline: String? = null
        var assignees: Collection<String>? = null
        var watchers: Collection<String>? = null
        var givenBy: String? = null
        val taskId: String = threadChannel.id
        var status: String? = null
        var priority: String? = null
        var completionDate: String? = null

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
                bot.messagesHandler.messages.taskEmbedCompletionDate -> completionDate = field.value
            }
        }

        return if (givenBy == null || status == null || priority == null)
            null
        else TaskData(taskId, givenBy, assignees, watchers, deadline, status, priority, completionDate)
    }

    /**
     * Checks if a given ThreadChannel is associated with a task owned by the bot.
     *
     * @param threadChannel The ThreadChannel object for which the ownership is being checked.
     * @return true if the provided ThreadChannel is owned by the bot, false otherwise.
     */
    fun isTask(threadChannel: ThreadChannel): Boolean {
        /**
         * Compare the owner ID of the provided threadChannel with the ID of the bot's user.
         * Return true if the IDs match (indicating ownership by the bot), false otherwise.
         */
        return threadChannel.ownerId == bot.jda.selfUser.id
    }

    /**
     * Retrieves the Task associated with a given MessageChannelUnion and Bot.
     *
     * @param bot The Bot instance used to check ownership and access the task manager.
     * @param channel The MessageChannelUnion object for which the associated Task is being retrieved.
     * @return The Task associated with the provided MessageChannelUnion and Bot, or null if no matching task is found.
     */
    fun getTask(bot: Bot, channel: MessageChannelUnion): Task? {
        /**
         * Check if the provided channel is a private or public thread channel. If not, return null.
         * Check if the provided channel is owned by the bot. If not, return null.
         * Search for a Task within the list of taskChannels' tasks based on a matching task ID (channel ID).
         * Return the first matching Task found, or null if no matching task is found.
         */
        if (channel.type != ChannelType.GUILD_PRIVATE_THREAD && channel.type != ChannelType.GUILD_PUBLIC_THREAD)
            return null

        if (channel.asThreadChannel().ownerId != bot.jda.selfUser.id) return null

        return bot.taskManager.taskChannels.flatMap { it.tasks }.find { it.taskData.taskId == channel.id }
    }

    /**
     * Retrieves the Task associated with a given ChannelUnion.
     *
     * @param channel The ChannelUnion object for which the associated Task is being retrieved.
     * @return The Task associated with the provided ChannelUnion, or null if no matching task is found.
     */
    fun getTask(channel: ChannelUnion): Task? {
        /**
         * Check if the provided channel is a private or public thread channel. If not, return null.
         * Check if the provided channel is a task thread by using the `isTask` function. If not, return null.
         * Search for a Task within the list of taskChannels' tasks based on a matching task ID (channel ID).
         * Return the first matching Task found, or null if no matching task is found.
         */
        if (channel.type != ChannelType.GUILD_PRIVATE_THREAD && channel.type != ChannelType.GUILD_PUBLIC_THREAD) return null

        if (!isTask(channel.asThreadChannel())) return null

        return taskChannels.flatMap { it.tasks }.find { it.taskData.taskId == channel.id }
    }


    /**
     * Retrieves the TaskChannel with a given channel ID.
     *
     * @param id The channel ID for which the associated TaskChannel is being retrieved.
     * @return The TaskChannel associated with the provided channel ID, or null if no matching channel is found.
     */
    fun getTaskChannel(id: String): TaskChannel? {
        /**
         * Search for a TaskChannel within the list of taskChannels based on a matching channel ID.
         * Return the first matching TaskChannel found, or null if no matching channel is found.
         */
        return taskChannels.find { it.channelId == id }
    }

    /**
     * Retrieves the TaskChannel associated with a given Task.
     *
     * @param task The Task object for which the associated TaskChannel is being retrieved.
     * @return The TaskChannel associated with the provided Task, or null if no matching channel is found.
     */
    fun getTaskChannel(task: Task): TaskChannel? {
        /**
         * Search for a TaskChannel within the list of taskChannels based on a matching task ID.
         * Return the first matching TaskChannel found, or null if no matching channel is found.
         */
        return taskChannels.find { taskChannel ->
            taskChannel.tasks.any {
                it.taskData.taskId == task.taskData.taskId
            }
        }
    }


    /**
     * Retrieves the TaskChannel associated with a given ThreadChannel.
     *
     * @param threadChannel The ThreadChannel object for which the associated TaskChannel is being retrieved.
     * @return The TaskChannel associated with the provided ThreadChannel, or null if no matching channel is found.
     */
    fun getTaskChannel(threadChannel: ThreadChannel): TaskChannel? {
        /**
         * Search for a TaskChannel within the list of taskChannels based on a matching parent channel ID.
         * Return the first matching TaskChannel found, or null if no matching channel is found.
         */
        return taskChannels.find { taskChannel ->
            taskChannel.channelId == threadChannel.parentChannel.id
        }
    }


    /**
     * Retrieves the TaskChannel associated with a given GuildChannelUnion.
     *
     * @param channel The GuildChannelUnion object for which the associated TaskChannel is being retrieved.
     * @return The TaskChannel associated with the provided GuildChannelUnion, or null if no matching channel is found.
     */
    fun getTaskChannel(channel: GuildChannelUnion): TaskChannel? {
        /**
         * Search for a TaskChannel within the list of taskChannels based on a matching channel ID.
         * Return the first matching TaskChannel found, or null if no matching channel is found.
         */
        return taskChannels.find { it.channelId == channel.id }
    }


    /**
     * Retrieves the TaskChannel associated with a given Role.
     *
     * @param role The Role object for which the associated TaskChannel is being retrieved.
     * @return The TaskChannel associated with the provided Role, or null if no matching channel is found.
     */
    fun getTaskChannel(role: Role): TaskChannel? {
        /**
         * Search for a TaskChannel within the list of taskChannels based on a matching role ID.
         * Return the first matching TaskChannel found, or null if no matching channel is found.
         */
        return taskChannels.find { taskChannel ->
            taskChannel.roles.contains(role.id)
        }
    }


    /**
     * Retrieves the TaskChannel associated with a given ForumChannel.
     *
     * @param forumChannel The ForumChannel object for which the associated TaskChannel is being retrieved.
     * @return The TaskChannel associated with the provided ForumChannel, or null if no matching channel is found.
     */
    fun getTaskChannel(forumChannel: ForumChannel): TaskChannel? {
        /**
         * Search for a TaskChannel within the list of taskChannels based on a matching channel ID.
         * Return the first matching TaskChannel found, or null if no matching channel is found.
         */
        return taskChannels.find { it.channelId == forumChannel.id }
    }


    /**
     * Retrieves the TaskChannel associated with a given member.
     *
     * @param member The Member object for which the associated TaskChannel is being retrieved.
     * @return The TaskChannel associated with the provided member, or null if no matching channel is found.
     */
    fun getTaskChannel(member: Member): TaskChannel? {
        /**
         * If the member has no roles, return null since there is no associated task channel.
         * Collect the role IDs of the member and search for a matching task channel based on those role IDs.
         * Return the first matching TaskChannel found, or null if no matching channel is found.
         */
        if (member.roles.isEmpty()) return null
        val roleIds = member.roles.mapTo(HashSet()) { it.id }
        return taskChannels.firstOrNull { channel -> channel.roles.any { roleIds.contains(it) } }
    }


    /**
     * Checks if a forum channel with tasks exists within a specified category.
     *
     * @param category The Category object representing the category to check.
     * @return A boolean indicating whether a forum channel with tasks exists within the specified category.
     */
    fun taskForumExists(category: Category): Boolean {
        /**
         * Check if any of the forum channels within the specified category have the same name as the tasks forum channel name in the bot's settings.
         * Return true if such a forum channel exists; otherwise, return false.
         */
        return category.forumChannels.any { it.name == bot.settingsHandler.settings.tasksForumChannelName }
    }


    /**
     * Refreshes the available and applied tags for thread channels in a specified task channel.
     *
     * @param bot The Bot instance managing the operation.
     * @param guild The Guild where the task channel and thread channels are located.
     * @param taskChannel The TaskChannel object representing the target task channel.
     * @return A CompletableDeferred<Unit> indicating the completion of the tag refreshing process.
     */
    fun refreshTags(bot: Bot, guild: Guild, taskChannel: TaskChannel): CompletableDeferred<Unit> {
        /**
         * Initialize a CompletableDeferred<Unit> to indicate the completion of the tag refreshing process.
         */
        val deferred = CompletableDeferred<Unit>()

        /**
         * Create a mutable list to store the ForumTagData objects for available tags.
         */
        val tags: MutableList<ForumTagData> = mutableListOf()

        /**
         * Add the defaultTagsData to the tags list.
         */
        tags.addAll(defaultTagsData)

        /**
         * Iterate through each role associated with the task channel in the guild.
         * For each member with the role, determine their name using nicknames if enabled, and add to tags list if not already present.
         */
        taskChannel.getRoles(guild).forEach { role ->
            guild.getMembersWithRoles(role).forEach { member ->
                val name = if (bot.settingsHandler.settings.nicknamesInTags && member.nickname != null)
                    member.nickname!! else member.effectiveName

                if (!tags.any { it.name == name }) {
                    tags.add(ForumTagData(member.id).setName(name))
                }
            }
        }

        /**
         * Create a HashMap to store applied tags for thread channels.
         */
        val appliedTags = HashMap<String, List<ForumTagData>>()

        /**
         * Iterate through each thread channel within the task channel.
         * If a thread channel has applied tags, add them to the appliedTags HashMap.
         */
        val channel = taskChannel.getChannel(guild)!!
        channel.threadChannels.forEach { threadChannel ->
            if (threadChannel.appliedTags.isNotEmpty()) {
                val tagList = threadChannel.appliedTags.map {
                    ForumTagData(it.name)
                }
                appliedTags[threadChannel.id] = tagList
            }
        }

        /**
         * Update the available tags for the task channel.
         * For each thread channel, update applied tags using the new tag IDs from the updated available tags.
         * Complete the deferred when all thread channels have been processed.
         */
        channel.manager.setAvailableTags(tags).queue {
            val newTagIds = channel.availableTags.associate { it.name to it.id }
            channel.threadChannels.forEach { threadChannel ->
                if (appliedTags.containsKey(threadChannel.id)) {
                    val toSnowflake = mutableListOf<ForumTagSnowflake>()
                    appliedTags[threadChannel.id]!!.map {
                        toSnowflake.add(ForumTagSnowflake.fromId(newTagIds[it.name]!!))
                    }
                    threadChannel.manager.setAppliedTags(toSnowflake).queue {
                        if (channel.threadChannels.last() == threadChannel)
                            deferred.complete(Unit)
                    }
                }
            }
        }

        /**
         * Return the CompletableDeferred indicating the completion of the tag refreshing process.
         */
        return deferred
    }


    /**
     * Retrieves a list of ForumTagSnowflake objects representing tags associated with a list of assignees.
     *
     * @param nicknameEnabled A boolean indicating whether nicknames are enabled.
     * @param forumChannel The ForumChannel object representing the forum channel associated with the assignees.
     * @param assignees A list of Member objects representing the assignees for which tags are being retrieved.
     * @return A mutable list of ForumTagSnowflake objects representing tags associated with the provided assignees.
     */
    fun getAssigneesAsTags(nicknameEnabled: Boolean, forumChannel: ForumChannel, assignees: List<Member>): MutableList<ForumTagSnowflake> {
        /**
         * Initialize an empty mutable list to store the ForumTagSnowflake objects.
         */
        val list = mutableListOf<ForumTagSnowflake>()

        /**
         * Retrieve the available tags from the specified forum channel.
         * Iterate through each assignee in the provided list and determine their name.
         * If nicknames are enabled and a member has a nickname, use the nickname; otherwise, use their effective name.
         * Find the matching tag with the same name in the available tags and add its corresponding ForumTagSnowflake to the list.
         */
        val availableTags = forumChannel.availableTags
        for (member in assignees) {
            val name = if (nicknameEnabled && member.nickname != null) member.nickname else member.effectiveName

            val matchingTag = availableTags.find { it.name == name }
            matchingTag?.let { list.add(ForumTagSnowflake.fromId(it.id)) }
        }

        /**
         * Return the completed mutable list containing ForumTagSnowflake objects representing tags associated with assignees.
         */
        return list
    }


    /**
     * Retrieves a list of ForumTagSnowflake objects representing tags associated with assignees in a task.
     *
     * @param task The Task object representing the task for which assignee tags are being retrieved.
     * @param forumChannel The ForumChannel object representing the forum channel associated with the task.
     * @return A mutable list of ForumTagSnowflake objects representing tags associated with task assignees.
     */
    fun getAssigneesTagsInTask(task: Task, forumChannel: ForumChannel): MutableList<ForumTagSnowflake> {
        /**
         * Initialize an empty mutable list to store the ForumTagSnowflake objects.
         */
        val list = mutableListOf<ForumTagSnowflake>()

        /**
         * Retrieve the available tags from the specified forum channel.
         * If the task does not have any assignees, return the empty list.
         */
        val availableTags = forumChannel.availableTags
        if (task.taskData.assignees == null) return list

        /**
         * Iterate through each assignee in the task and determine their name.
         * If nicknames are enabled and a member has a nickname, use the nickname; otherwise, use their effective name.
         * Find the matching tag with the same name in the available tags and add its corresponding ForumTagSnowflake to the list.
         */
        for (member in task.getAssignees()) {
            val name = if (bot.settingsHandler.settings.nicknamesInTags && member.nickname != null) member.nickname else member.effectiveName

            val matchingTag = availableTags.find { it.name == name }
            matchingTag?.let { list.add(ForumTagSnowflake.fromId(it.id)) }
        }

        /**
         * Return the completed mutable list containing ForumTagSnowflake objects representing assignee tags.
         */
        return list
    }


    /**
     * Generates an EmbedBuilder for displaying information about a task.
     *
     * @param title The title to be displayed in the embed.
     * @param description The description to be displayed in the embed.
     * @param task The Task object representing the task for which the embed is being generated.
     * @return An EmbedBuilder containing information about the specified task.
     */
    fun generateTaskEmbed(title: String, description: String, task: Task): EmbedBuilder {
        /**
         * Create an EmbedBuilder with a title, description, and blue color.
         */
        val builder = EmbedBuilder()
            .setTitle(title)
            .setDescription(description)
            .setColor(Color.BLUE)

        /**
         * Check if the task has a deadline and add it as a field in the embed.
         */
        val deadline = task.deadline
        if (deadline != null) {
            builder.addField(MessageEmbed.Field(bot.messagesHandler.messages.taskEmbedDeadline,
                bot.taskManager.taskDateFormat.format(deadline), true))
        }

        /**
         * Check if the task has assignees and add them as a field in the embed.
         */
        val assignees = task.getAssignees()
        if (assignees.isNotEmpty()) {
            val assigneesString = assignees.joinToString(",") { it.asMention }
            builder.addField(MessageEmbed.Field(
                bot.messagesHandler.messages.taskEmbedAssignees, assigneesString, true))
        }

        /**
         * Check if the task has watchers and add them as a field in the embed.
         */
        val watchers = task.getWatchers()
        if (watchers.isNotEmpty()) {
            val watchersString = watchers.joinToString(",") { it.asMention }
            builder.addField(MessageEmbed.Field(
                bot.messagesHandler.messages.taskEmbedWatchers, watchersString, true))
        }

        /**
         * Add the priority, status, task giver, and completion date as fields in the embed.
         */
        val priority = task.priority.toString().replace("_", "")
        builder.addField(MessageEmbed.Field(
            bot.messagesHandler.messages.taskEmbedPriority, priority, true))

        val status = task.status.toString().replace("_", " ")
        builder.addField(MessageEmbed.Field(
            bot.messagesHandler.messages.taskEmbedStatus, status, true))

        val taskGiver = task.getTaskGiver()
        if (taskGiver != null) {
            builder.addField(MessageEmbed.Field(
                bot.messagesHandler.messages.taskEmbedGivenBy, taskGiver.asMention, true))
        }

        val completionDate = task.taskData.completionDate
        if (completionDate != null) {
            builder.addField(
                MessageEmbed.Field(bot.messagesHandler.messages.taskEmbedCompletionDate,
                    completionDate, true))
        }

        /**
         * Return the completed EmbedBuilder containing information about the task.
         */
        return builder
    }


    /**
     * Retrieves the corresponding forum tag for a specific task status within a given forum channel.
     *
     * @param forumChannel The ForumChannel object representing the target forum channel.
     * @param status The TaskStatus for which the forum tag is being retrieved.
     * @return The corresponding ForumTag for the specified task status within the forum channel, or null if not found.
     */
    fun getTag(forumChannel: ForumChannel, status: TaskStatus): ForumTag? {
        /**
         * Determine the name of the forum tag based on the provided task status.
         * Find and return the first forum tag that matches the determined name within the available tags of the forum channel.
         * Return null if no matching forum tag is found.
         */
        return when (status) {
            TaskStatus.OPEN -> forumChannel.availableTags.firstOrNull { it.name == bot.settingsHandler.settings.openTag }
            TaskStatus.IN_PROGRESS -> forumChannel.availableTags.firstOrNull { it.name == bot.settingsHandler.settings.inProgressTag }
            TaskStatus.DONE -> forumChannel.availableTags.firstOrNull { it.name == bot.settingsHandler.settings.doneTag }
        }
    }

    /**
     * Retrieves the count of tasks with a specific status within a specific task channel.
     *
     * @param taskChannel The TaskChannel object representing the target task channel.
     * @param status The TaskStatus for which the task count is being retrieved.
     * @return The count of tasks with the specified status within the specified task channel.
     */
    fun getTaskCount(taskChannel: TaskChannel, status: TaskStatus): Int {
        /**
         * Count the tasks within the specified task channel that match the specified status.
         * Return the count of tasks with the specified status within the specified task channel.
         */
        return taskChannel.tasks.count { it.status == status }
    }


    /**
     * Retrieves the total count of tasks within a specific task channel.
     *
     * @param taskChannel The TaskChannel object representing the target task channel.
     * @return The total count of tasks within the specified task channel.
     */
    fun getTaskCount(taskChannel: TaskChannel): Int {
        /**
         * Return the total count of tasks within the specified task channel.
         */
        return taskChannel.tasks.size
    }


    /**
     * Retrieves the total count of tasks in a task channel associated with a given role.
     *
     * @param role The Role object representing the target role.
     * @return The total count of tasks in the associated task channel of the role.
     *         Returns 0 if no associated task channel is found.
     */
    fun getTaskCount(role: Role): Int {
        /**
         * Retrieve the associated task channel for the given role.
         * If an associated task channel is found, return the total count of tasks in that channel.
         * If no associated task channel is found, return 0 indicating no tasks for the role.
         */
        val taskChannel = getTaskChannel(role) ?: return 0
        return taskChannel.tasks.size
    }


    /**
     * Retrieves the count of tasks with a specific status in a task channel associated with a given role.
     *
     * @param role The Role object representing the target role.
     * @param status The TaskStatus for which the task count is being retrieved.
     * @return The count of tasks with the specified status in the associated task channel of the role.
     *         Returns 0 if no associated task channel is found.
     */
    fun getTaskCount(role: Role, status: TaskStatus): Int {
        /**
         * Retrieve the associated task channel for the given role.
         * If an associated task channel is found, count the tasks with the specified status in that channel.
         * If no associated task channel is found, return 0 indicating no tasks for the role.
         */
        val taskChannel = getTaskChannel(role) ?: return 0
        return taskChannel.tasks.count { it.status == status }
    }


    /**
     * Retrieves the total count of tasks in a given forum channel's associated task channel.
     *
     * @param forumChannel The ForumChannel object representing the target forum channel.
     * @return The total count of tasks in the associated task channel of the forum channel.
     *         Returns 0 if no associated task channel is found.
     */
    fun getTaskCount(forumChannel: ForumChannel): Int {
        /**
         * Retrieve the associated task channel for the given forum channel.
         * If an associated task channel is found, return the total count of tasks in that channel.
         * If no associated task channel is found, return 0 indicating no tasks for the forum channel.
         */
        val taskChannel = getTaskChannel(forumChannel) ?: return 0
        return taskChannel.tasks.size
    }


    /**
     * Retrieves the count of tasks with a specific status in a given forum channel's associated task channel.
     *
     * @param forumChannel The ForumChannel object representing the target forum channel.
     * @param status The TaskStatus for which the task count is being retrieved.
     * @return The count of tasks with the specified status in the associated task channel of the forum channel.
     *         Returns 0 if no associated task channel is found.
     */
    fun getTaskCount(forumChannel: ForumChannel, status: TaskStatus): Int {
        /**
         * Retrieve the associated task channel for the given forum channel.
         * If an associated task channel is found, count the tasks with the specified status in that channel.
         * If no associated task channel is found, return 0 indicating no tasks for the forum channel.
         */
        val taskChannel = getTaskChannel(forumChannel) ?: return 0
        return taskChannel.tasks.count { it.status == status }
    }


    /**
     * Retrieves the count of tasks assigned to a given member across their assigned task channel.
     *
     * @param member The Member object representing the target member.
     * @return The count of tasks assigned to the member in their assigned task channel, or 0 if no channel is found.
     */
    fun getTaskCount(member: Member): Int {
        /**
         * Retrieve the task channel associated with the member.
         * If a task channel is found, count the tasks assigned to the member in that channel.
         * If no task channel is found, return 0 indicating no tasks for the member.
         */
        return getTaskChannel(member)?.let { taskChannel ->
            taskChannel.tasks.count { it.taskData.assignees?.contains(member.id) == true }
        } ?: 0
    }

    /**
     * Retrieves the count of tasks with a specific status assigned to a given member across all task channels.
     *
     * @param member The Member object representing the target member.
     * @param status The TaskStatus for which the task count is being retrieved.
     * @return The count of tasks with the specified status assigned to the member across all task channels.
     */
    fun getTaskCount(member: Member, status: TaskStatus): Int {
        /**
         * Sum up the counts of tasks with the specified status that are assigned to the member
         * across all task channels.
         */
        return taskChannels.sumOf { channel ->
            channel.tasks.count { task ->
                task.taskData.assignees?.contains(member.id) == true && task.status == status
            }
        }
    }


    /**
     * Retrieves the total count of tasks across all task channels.
     *
     * @return The total count of tasks across all available task channels.
     */
    fun getTaskCount(): Int {
        /**
         * Sum up the sizes of all tasks in all task channels.
         * Return the total count of tasks across all available task channels.
         */
        return taskChannels.sumOf { it.tasks.size }
    }


    /**
     * Retrieves the count of tasks with a specific status across all task channels.
     *
     * @param status The TaskStatus for which the task count is being retrieved.
     * @return The count of tasks with the specified status across all task channels.
     */
    fun getTaskCount(status: TaskStatus): Int {
        /**
         * Retrieve all tasks from all task channels and count the tasks that match the specified status.
         * Return the count of tasks with the specified status across all task channels.
         */
        return taskChannels.flatMap { it.tasks }.count { it.status == status }
    }


    /**
     * Refreshes the embed message of a task by updating its content based on the task's current state.
     *
     * @param task The Task object representing the task whose embed message needs to be refreshed.
     */
    fun refreshTaskEmbed(task: Task) {
        /**
         * Retrieve the channel associated with the task and retrieve its starting message.
         * Queue a callback to edit the existing task embed with a new one generated based on the updated task.
         */
        task.getChannel()?.retrieveStartMessage()?.queue { startMessage ->
            /**
             * Retrieve the first embed from the existing message and generate a new task embed using updated data.
             * Edit the starting message with the new task embed to visually refresh the task's representation.
             */
            val embed = startMessage.embeds[0]
            startMessage.editMessageEmbeds(generateTaskEmbed(embed.title!!, embed.description!!, task).build()).queue()
        }
    }

    /**
     * Checks whether a given member is considered a head within a specific task channel.
     *
     * @param taskChannel The TaskChannel object representing the target task channel.
     * @param member The Member object representing the potential head.
     * @return True if the member has any role that is considered a head within the task channel, false otherwise.
     */
    fun isHead(taskChannel: TaskChannel, member: Member): Boolean {
        /**
         * Check if the member has any role that matches the headRoleIds defined in the task channel.
         * Return true if the member has such a role, indicating they are considered a head within the task channel.
         * Return false if the member does not have any matching role.
         */
        return member.roles.any { taskChannel.headRoleIds.contains(it.id) }
    }

    /**
     * Retrieves a list of tasks that have passed their deadlines and are not marked as "DONE".
     *
     * @return A list of Task objects that have passed their deadlines and are not marked as "DONE".
     */
    fun getTasksPassedDeadline(): List<Task> {
        /**
         * Check if there are no task channels available.
         * If so, return an empty list since there are no tasks to consider.
         */
        if (taskChannels.isEmpty()) {
            return emptyList()
        }

        /**
         * Retrieve all tasks from all task channels, then filter for tasks that are not marked as "DONE".
         * Further filter tasks with deadlines that have passed based on the afterDeadline function.
         * Return a list of tasks that have passed their deadlines and are not marked as "DONE".
         */
        return taskChannels.flatMap { it.tasks }
            .filter { it.status != TaskStatus.DONE }
            .filter { it.deadline != null && afterDeadline(taskDateFormat.format(it.deadline)) }
    }

    /**
     * Notifies relevant individuals about a task that has passed its deadline.
     *
     * @param task The Task object representing the task that has passed its deadline.
     */
    fun notifyPassedDeadline(task: Task) {
        /**
         * Retrieve the channel associated with the task.
         */
        val channel = task.getChannel() ?: return

        /**
         * Create a comma-separated string of mentions for assignees and watchers of the task.
         */
        val assignees = task.getAssignees().joinToString(",") { it.asMention }
        val watchers = task.getWatchers().joinToString(",") { it.asMention }

        /**
         * Send a message to the task's channel indicating that the deadline has passed.
         */
        channel.sendMessage(bot.messagesHandler.messages.deadlinePast
            .replace("%mentions%", "$assignees , $watchers")).queue()

        /**
         * If enabled in settings, notify assignees individually about the passed deadline.
         */
        if (bot.settingsHandler.settings.notifyAssigneesPassedDeadline) {
            task.getAssignees().forEach {
                it.user.openPrivateChannel().flatMap { privateChannel ->
                    privateChannel.sendMessage(bot.messagesHandler.messages.notifyAssigneePassedDeadline
                        .replace("%channel%", channel.asMention))
                }.queue()
            }
        }

        /**
         * If enabled in settings, notify watchers individually about the passed deadline.
         */
        if (bot.settingsHandler.settings.notifyWatchersPassedDeadline) {
            task.getWatchers().forEach {
                it.user.openPrivateChannel().flatMap { privateChannel ->
                    privateChannel.sendMessage(bot.messagesHandler.messages.notifyWatchersPassedDeadline
                        .replace("%channel%", channel.asMention))
                }.queue()
            }
        }
    }

    /**
     * Starts a recurring coroutine job that checks for tasks that have passed their deadlines and sends notifications.
     *
     * @return A Job object representing the running coroutine job.
     */
    fun startDeadlineCheck(): Job {
        /**
         * Launch a coroutine within a CoroutineScope with a default dispatcher (thread pool).
         * The coroutine continuously checks for tasks that have passed their deadlines and sends notifications.
         * It delays for a day's interval between checks.
         *
         * @return A Job object representing the running coroutine job.
         */
        return CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                // Retrieve tasks that have passed their deadlines.
                getTasksPassedDeadline().forEach {
                    // Notify about tasks that have passed their deadlines.
                    notifyPassedDeadline(it)
                }

                // Delay for a day's interval before the next round of checks.
                delay(((1000 * 60) * 60) * 24)
            }
        }
    }

    /**
     * Marks a task as complete by updating its status, completion date, and applying appropriate tags.
     *
     * @param task The Task object representing the task to be marked as complete.
     */
    fun markComplete(task: Task) {
        /**
         * Retrieve the channel associated with the task.
         */
        val channel = bot.jda.getThreadChannelById(task.taskData.taskId) ?: return

        /**
         * Convert the channel to a ForumChannel object to access its properties.
         */
        val forumChannel = channel.parentChannel.asForumChannel()

        /**
         * Retrieve the available tags in the forum channel and create a map of tag names to their IDs.
         */
        val availableTags = forumChannel.availableTags.associate { it.name to it.id }

        /**
         * Create a list to store the tags to be applied to the task.
         */
        val tags: MutableList<ForumTagSnowflake> = mutableListOf()

        /**
         * Retrieve the assignees of the task.
         */
        val assignees = task.getAssignees()

        /**
         * Iterate through available tags and assignee members to determine tags to be applied.
         */
        availableTags.forEach { tag ->
            assignees.forEach {
                if (tag.key == getNicknameOrName(it)) {
                    tags.add(ForumTagSnowflake.fromId(tag.value))
                }
            }
        }

        /**
         * Add the "done" tag to indicate completion.
         */
        tags.add(ForumTagSnowflake.fromId(availableTags[bot.settingsHandler.settings.doneTag]!!))

        /**
         * Set the task's completion date, status, and task data status to "DONE".
         */
        task.taskData.completionDate = taskDateFormat.format(Date())
        task.status = TaskStatus.DONE
        task.taskData.status = TaskStatus.DONE.toString()

        /**
         * Retrieve the starting message of the channel and update it with the new task embed.
         */
        channel.retrieveStartMessage().queue {
            val embed = it.embeds[0]
            it.editMessageEmbeds(generateTaskEmbed(embed.title!!, embed.description!!, task).build()).queue {
                /**
                 * Apply the generated tags to the channel and mark it as archived.
                 */
                channel.manager.setAppliedTags(tags).queue {
                    channel.manager.setArchived(true).queue()
                }
            }
        }
    }

    /**
     * Retrieves the nickname of a member if enabled in settings, otherwise returns their effective name.
     *
     * @param member The Member object for which the nickname or name is being retrieved.
     * @return The nickname of the member if available and enabled, otherwise their effective name.
     */
    private fun getNicknameOrName(member: Member): String {
        /**
         * Check if nicknames in tags are enabled in the bot's settings.
         * If enabled and the member has a nickname, return the member's nickname.
         * Otherwise, return the member's effective (display) name.
         */
        return if (bot.settingsHandler.settings.nicknamesInTags && member.nickname != null) {
            member.nickname!!
        } else {
            member.effectiveName
        }
    }

    /**
     * Generates TaskerStats for a specific member based on their assigned tasks in task channels.
     *
     * @param member The Member object for which statistics are being generated.
     * @return A TaskerStats object containing the generated statistics for the specified member.
     */
    fun getStats(member: Member): TaskerStats {
        // Create a new TaskerStats instance for the specified member.
        val stats = TaskerStats(member.id)

        // Iterate through each task in all task channels and check if the member is an assignee.
        // If the member is an assignee, classify the task to update the statistics.
        for (task in taskChannels.flatMap { it.tasks }) {
            val assignees = task.taskData.assignees ?: continue
            if (member.id in assignees) {
                classifyTask(stats, task)
            }
        }

        // Return the TaskerStats object containing the generated statistics for the specified member.
        return stats
    }

    /**
     * Generates TaskerStats for a specific role based on task classification in task channels.
     *
     * @param role The Role object for which statistics are being generated.
     * @return A TaskerStats object containing the generated statistics for the specified role.
     */
    fun getStats(role: Role): TaskerStats {
        // Create a new TaskerStats instance for the specified role.
        val stats = TaskerStats(role.id)

        // Filter task channels to find those associated with the specified role and containing tasks.
        // Then, iterate through each task in these channels and classify them to update the statistics.
        taskChannels
            .filter { it.roles.contains(role.id) && it.tasks.isNotEmpty() }
            .flatMap { it.tasks }
            .forEach { classifyTask(stats, it) }

        // Return the TaskerStats object containing the generated statistics for the specified role.
        return stats
    }

    /**
     * Creates an EmbedBuilder containing statistics information for a specific member.
     *
     * @param name The name of the member for whom the statistics are being displayed.
     * @param stats The TaskerStats object containing the statistics information.
     * @return An EmbedBuilder containing formatted statistics information for the specified member.
     */
    fun getStatsEmbed(name: String, stats: TaskerStats): EmbedBuilder {
        // Create a new EmbedBuilder to build the embed message.
        val builder = EmbedBuilder()

        // Set the title of the embed using a formatted message, replacing %member% with the provided name.
        builder.setTitle(bot.messagesHandler.messages.statsTitle.replace("%member%", name))

        // Create a StringBuilder to accumulate the statistics information.
        val sb = StringBuilder()

        // Append various statistics data to the StringBuilder with formatted messages.
        sb.append(bot.messagesHandler.messages.statsActiveTasks.replace("%count%", "${stats.activeTasks.size}"))
            .append("\n")
            .append(bot.messagesHandler.messages.statsInProgressTasks.replace("%count%", "${stats.inProgressTasks.size}"))
            .append("\n")
            .append(bot.messagesHandler.messages.statsTotalCompleted.replace("%count%", "${stats.completedTasks.size}"))
            .append("\n")
            .append(bot.messagesHandler.messages.statsCompletedOnTime.replace("%count%", "${stats.completedOnTime.size}"))
            .append("\n")
            .append(bot.messagesHandler.messages.statsDelayedTasks.replace("%count%", "${stats.completedAfterDeadline.size}"))

        // Set the description of the embed using the accumulated statistics information.
        builder.setDescription(sb.toString())

        // Return the built EmbedBuilder containing the statistics information.
        return builder
    }

    /**
     * This function is responsible for classifying tasks based on their status and completion date.
     * It updates the statistics (TaskerStats) object with information about the tasks' statuses.
     *
     * @param stats The TaskerStats object containing statistics to be updated.
     * @param task The Task object representing the task to be classified.
     */
    private fun classifyTask(stats: TaskerStats, task: Task) {
        when (task.status) {
            TaskStatus.OPEN, TaskStatus.IN_PROGRESS -> {
                // For OPEN and IN_PROGRESS tasks, add their task IDs to the list of active tasks.
                stats.activeTasks.add(task.taskData.taskId)
            }
            TaskStatus.DONE -> {
                // For DONE tasks, check if the task has a completion date.
                if (task.taskData.completionDate != null) {
                    // If the task was completed after the deadline, add its task ID to the list of completed tasks after the deadline.
                    if (afterDeadline(task.taskData.completionDate!!)) {
                        stats.completedAfterDeadline.add(task.taskData.taskId)
                    } else {
                        // If the task was completed on time, add its task ID to the list of completed tasks on time.
                        stats.completedOnTime.add(task.taskData.taskId)
                    }
                }
                // Regardless of completion time, add the task ID to the list of all completed tasks.
                stats.completedTasks.add(task.taskData.taskId)
            }
        }
    }

    /**
     * Checks if the current date is after a given deadline date.
     *
     * @param deadlineStr The string representation of the deadline date in the format "dd-MM-yyyy".
     * @return True if the current date is after the deadline, false otherwise.
     */
    fun afterDeadline(deadlineStr: String): Boolean {
        // Create a date format for parsing the deadline string.
        val dateFormat = SimpleDateFormat("dd-MM-yyyy")

        // Get the current date and time.
        val currentDate = Calendar.getInstance()

        // Initialize a Calendar instance for the deadline.
        val deadline = Calendar.getInstance()

        // Set the current date and time for both current and deadline Calendars.
        currentDate.time = Date()
        deadline.time = dateFormat.parse(deadlineStr)

        // Extract year, month, and day components from the current date.
        val currentYear = currentDate.get(Calendar.YEAR)
        val currentMonth = currentDate.get(Calendar.MONTH)
        val currentDay = currentDate.get(Calendar.DAY_OF_MONTH)

        // Extract year, month, and day components from the deadline date.
        val deadlineYear = deadline.get(Calendar.YEAR)
        val deadlineMonth = deadline.get(Calendar.MONTH)
        val deadlineDay = deadline.get(Calendar.DAY_OF_MONTH)

        // Compare current date components with deadline date components.
        if (currentYear > deadlineYear) {
            return true
        } else if (currentYear == deadlineYear && currentMonth > deadlineMonth) {
            return true
        } else if (currentYear == deadlineYear && currentMonth == deadlineMonth && currentDay > deadlineDay) {
            return true
        }

        // Return false if the current date is on or before the deadline.
        return false
    }

    /**
     * Checks whether a given member is an assignee for a specific task.
     *
     * @param member The Member object representing the potential assignee.
     * @param task The Task object representing the task to be checked.
     * @return True if the member is an assignee for the task, false otherwise.
     */
    fun isAssignee(member: Member, task: Task): Boolean {
        /**
         * If the task's assignees list is null, the member cannot be an assignee.
         * Return false to indicate that the member is not an assignee.
         */
        if (task.taskData.assignees == null) {
            return false
        }

        /**
         * Check if the member's ID is present in the list of assignees' IDs for the task.
         * Return true if the member's ID is found in the list, indicating the member is an assignee.
         * Return false if the member's ID is not found in the list.
         */
        return task.taskData.assignees!!.contains(member.id)
    }

    /**
     * Checks whether a given member is a watcher for a specific task.
     *
     * @param member The Member object representing the potential watcher.
     * @param task The Task object representing the task to be checked.
     * @return True if the member is a watcher for the task, false otherwise.
     */
    fun isWatcher(member: Member, task: Task): Boolean {
        /**
         * If the task's watchers list is null, the member cannot be a watcher.
         * Return false to indicate that the member is not a watcher.
         */
        if (task.taskData.watchers == null) {
            return false
        }

        /**
         * Check if the member's ID is present in the list of watchers' IDs for the task.
         * Return true if the member's ID is found in the list, indicating the member is a watcher.
         * Return false if the member's ID is not found in the list.
         */
        return task.taskData.watchers!!.contains(member.id)
    }

    /**
     * Retrieves a corresponding ForumTagSnowflake for a given TaskStatus within a specific ForumChannel.
     *
     * @param forumChannel The ForumChannel object representing the target forum channel.
     * @param status The TaskStatus value for which the corresponding forum tag is needed.
     * @return A ForumTagSnowflake object representing the forum tag associated with the provided status, or null if not found.
     */
    fun getStatusAsTag(forumChannel: ForumChannel, status: TaskStatus): ForumTagSnowflake? {
        /**
         * Determine the name of the forum tag associated with the provided TaskStatus.
         * The name is retrieved based on the TaskStatus from the bot's settingsHandler.
         */
        val statusName = when (status) {
            TaskStatus.OPEN -> bot.settingsHandler.settings.openTag
            TaskStatus.DONE -> bot.settingsHandler.settings.doneTag
            TaskStatus.IN_PROGRESS -> bot.settingsHandler.settings.inProgressTag
        }

        /**
         * Find the available forum tag with the same name as the determined statusName.
         * Return the matching ForumTagSnowflake object if found, or null if not found.
         */
        return forumChannel.availableTags.find { it.name == statusName }
    }

    /**
     * Updates the status of a task and applies corresponding tags to its associated forum channel.
     *
     * @param task The Task object representing the task to be updated.
     * @param status The TaskStatus value to which the task's status should be updated.
     * @return True if the status update and tag application were successful, false otherwise.
     */
    fun updateStatus(task: Task, status: TaskStatus): Boolean {
        /**
         * Get the forum channel associated with the task and convert it to a ForumChannel object.
         * This allows interaction with the forum channel's properties and tags.
         */
        val forumChannel = task.getChannel()!!.parentChannel.asForumChannel()

        /**
         * Retrieve the forum tag associated with the provided TaskStatus within the given forum channel.
         * If no matching tag is found, return false to indicate that the update and tag application were not successful.
         */
        val statusTag = getStatusAsTag(forumChannel, status) ?: return false

        /**
         * For OPEN or IN_PROGRESS statuses, clear the task's completion date if it is not null.
         * This ensures that the completion date is reset when the task status changes to OPEN or IN_PROGRESS.
         */
        if (status == TaskStatus.OPEN || status == TaskStatus.IN_PROGRESS) {
            if (task.taskData.completionDate != null) {
                task.taskData.completionDate = null
            }
        }

        /**
         * Update the task's status in both the task data and the task object itself.
         * Refresh the task's embed to reflect the updated status visually.
         */
        task.taskData.status = status.toString()
        task.status = status
        refreshTaskEmbed(task)

        /**
         * Retrieve the tags of the task's assignees within the forum channel.
         * Add the status tag to the assignee tags list.
         * Apply the updated list of tags to the task's channel manager.
         */
        val assigneeTags = getAssigneesTagsInTask(task, forumChannel)
        assigneeTags.add(statusTag)
        task.getChannel()!!.manager.setAppliedTags(assigneeTags).queue()

        /**
         * Return true to indicate that the status update and tag application were successful.
         */
        return true
    }

}