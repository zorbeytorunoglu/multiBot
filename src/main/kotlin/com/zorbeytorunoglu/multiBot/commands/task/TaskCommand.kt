package com.zorbeytorunoglu.multiBot.commands.task

import com.zorbeytorunoglu.multiBot.Bot
import com.zorbeytorunoglu.multiBot.commands.Command
import com.zorbeytorunoglu.multiBot.task.*
import com.zorbeytorunoglu.multiBot.utils.Utils
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.forums.ForumTagData
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditData
import java.text.ParseException

class TaskCommand(private val bot: Bot): Command {

    override val name: String
        get() = bot.commandsConfigurationHandler.commands.taskCmd
    override val description: String
        get() = bot.commandsConfigurationHandler.commands.taskDesc
    override val guildOnly: Boolean
        get() = true

    override fun optionData(): Collection<OptionData> {
        return emptyList()
    }

    // /task delete
    // /task set status <status>
    // /task set assignee <members>
    // /task set deadline <deadline>
    // /task set watcher <members>
    // /task set priority <priority>
    // /task set head <role>
    // /task count <role>

    // /task setup <category> <roles>

    override fun subcommandData(): Collection<SubcommandData> {

        val priorityChoices = TaskPriority.values().map {
            Choice(it.name, it.name)
        }

        val statusChoices = mutableListOf(Choice("ALL", "ALL"))

        val status = TaskStatus.values().map {
            Choice(it.name, it.name)
        }

        statusChoices.addAll(status)

        val setSubCommandsChoices = listOf("status", "assignee", "deadline", "watcher", "priority").map {
            Choice(it, it)
        }

        return listOf(
            SubcommandData("stats", "Gets the stats of a tasker.").addOptions(
                OptionData(OptionType.USER, "member", "Member", false, false),
                OptionData(OptionType.ROLE, "role", "Role", false, false)
            ),
            SubcommandData("count", "Gets the task count.")
                .addOptions(
                    OptionData(OptionType.STRING, "status","Task status",true, false)
                        .addChoices(statusChoices),
                    OptionData(OptionType.CHANNEL, "channel", "Task channel", false, false),
                    OptionData(OptionType.USER, "member", "Member", false, false),
                    OptionData(OptionType.ROLE,"role", "Role", false, false)
                ),
            SubcommandData("delete","Deletes the task."),
            SubcommandData("set", "Set a value in a task.")
                .addOptions(OptionData(OptionType.STRING, "key", "Key", true, false)
                    .addChoices(setSubCommandsChoices),
                    OptionData(OptionType.STRING, "value", "Value", true, false)
                ),
            SubcommandData("setup", "Sets up a task channel for a role/department.")
                .addOptions(
                    OptionData(OptionType.STRING,"category", "ID of the category.", true, false),
                    OptionData(OptionType.STRING, "roles", "IDs/names of the roles/departments. Separate them with ','", true, false),
                    OptionData(OptionType.STRING, "heads", "Head roles of the department.", true, false)
                ),
            SubcommandData("create", "Creates a task.")
                .addOptions(
                    OptionData(OptionType.CHANNEL, "channel", "Task channel", true, false),
                    OptionData(OptionType.STRING, "title", "Title of the task", true, false),
                    OptionData(OptionType.STRING, "description", "Description of the task", true, false),
                    OptionData(OptionType.STRING, "assignees", "Assignees", false, false),
                    OptionData(OptionType.STRING, "deadline", "Deadline of the task (dd-MM-yyyy)", false, false),
                    OptionData(OptionType.STRING, "watchers", "Watchers", false, false),
                    OptionData(OptionType.STRING, "priority", "Priority", false, false)
                        .addChoices(priorityChoices)
                ),
            SubcommandData("refreshtags", "Refreshes tags.").addOptions(
                OptionData(OptionType.CHANNEL, "channel", "Task channel", true, false)
            )
        )
    }

    override fun execute(event: SlashCommandInteractionEvent) {

        if (event.subcommandName == "stats") {

            if (event.options.isEmpty()) {
                event.reply(bot.messagesHandler.messages.wrongStatsUsage).queue()
                return
            }

            if (event.getOption("member") != null) {

                val member = event.getOption("member")!!.asMember

                var name = member!!.effectiveName

                if (member.nickname != null)
                    name = member.nickname!!

                event.replyEmbeds(bot.taskManager.getStatsEmbed(name, bot.taskManager.getStats(member)).build()).queue()

                return

            } else if (event.getOption("role") != null) {

                val role = event.getOption("role")!!.asRole

                event.replyEmbeds(bot.taskManager.getStatsEmbed(role.name, bot.taskManager.getStats(role)).build()).queue()

                return

            } else {

                event.reply(bot.messagesHandler.messages.wrongStatsUsage).queue()
                return

            }

        }

        if (event.subcommandName == "count") {

            val statusString = event.getOption("status")!!.asString

            val status: TaskStatus? = try {
                TaskStatus.valueOf(statusString)
            } catch (e: IllegalArgumentException) {
                null
            }

            val statusMessage = when (status) {

                null -> bot.messagesHandler.messages.all
                TaskStatus.DONE -> bot.settingsHandler.settings.doneTag
                TaskStatus.IN_PROGRESS -> bot.settingsHandler.settings.inProgressTag
                TaskStatus.OPEN -> bot.settingsHandler.settings.openTag

            }

            if (event.getOption("member") != null) {

                val member = event.guild!!.getMember(event.getOption("member")!!.asUser)!!

                event.reply(getTaskCountMessage(member.asMention,
                    if (status != null) bot.taskManager.getTaskCount(member, status) else
                        bot.taskManager.getTaskCount(member), statusMessage)).queue()

                return

            } else if (event.getOption("role") != null) {

                val role = event.getOption("role")!!.asRole

                event.reply(getTaskCountMessage(role.name,
                    if (status != null) bot.taskManager.getTaskCount(role, status) else
                        bot.taskManager.getTaskCount(role),
                    statusMessage)).queue()

                return

            } else if (event.getOption("channel") != null) {

                val channel = event.getOption("channel")!!.asChannel

                val taskChannel = bot.taskManager.getTaskChannel(
                    channel
                ) ?: run {
                    event.reply(bot.messagesHandler.messages.taskChannelNotFound).queue()
                    return
                }

                event.reply(getTaskCountMessage(channel.asMention,
                    if (status != null) bot.taskManager.getTaskCount(taskChannel, status)
                    else bot.taskManager.getTaskCount(taskChannel),
                    statusMessage)).queue()

                return

            } else {

                event.reply(bot.messagesHandler.messages.totalTaskCount.replace("%count%",
                    if (status != null) "${bot.taskManager.getTaskCount(status)}" else
                "${bot.taskManager.getTaskCount()}")).queue()

                return

            }

        }

        if (event.subcommandName == "set") {

            if (!bot.permissionManager.hasPermission(event.member!!,com.zorbeytorunoglu.multiBot.permissions.Permission.TASK_SET)) {
                event.reply(bot.messagesHandler.messages.noPermission).setEphemeral(true).queue()
                return
            }

            val key = event.getOption("key")!!.asString

            val value = event.getOption("value")!!.asString

            val task = bot.taskManager.getTask(bot, event.channel) ?: run {
                event.channel.sendMessage(bot.messagesHandler.messages.taskNotFound).queue()
                return
            }

            if (key == "assignee") {

                val members = Utils.getMembers(event.guild!!, removeSpaces(value)!!.split(","))

                if (members.isEmpty()) {
                    event.channel.sendMessage(bot.messagesHandler.messages.taskSetMembersNotFound).queue()
                    return
                }

                task.taskData.assignees = members.map { it.id }

                bot.taskManager.refreshTaskEmbed(event.channel.asThreadChannel(), task)

                event.channel.sendMessage(bot.messagesHandler.messages.assigneeSet).queue()

                return

            }

            if (key == "status") {

                val status: TaskStatus = if (value.equals("in progress", true)) {
                    TaskStatus.IN_PROGRESS
                } else if (value.equals("open", true)) {
                    TaskStatus.OPEN
                } else if (value.equals("done", true)) {
                    TaskStatus.DONE
                } else {
                    event.channel.sendMessage(bot.messagesHandler.messages.invalidStatus).queue()
                    return
                }

                task.taskData.status = status.toString()
                task.status = status

                bot.taskManager.refreshTaskEmbed(event.channel.asThreadChannel(), task)

                event.channel.sendMessage(bot.messagesHandler.messages.statusSet).queue()

                return

            }

            if (key == "deadline") {

                val date = try {
                    bot.taskManager.taskDateFormat.parse(value)
                } catch (e: ParseException) {
                    e.printStackTrace()
                    event.channel.sendMessage(bot.messagesHandler.messages.invalidDate).queue()
                    return
                }

                task.deadline = date
                task.taskData.deadline = value

                bot.taskManager.refreshTaskEmbed(event.channel.asThreadChannel(), task)

                event.channel.sendMessage(bot.messagesHandler.messages.deadlineSet).queue()

                return

            }

            if (key == "watcher") {

                val watchers = Utils.getMembers(event.guild!!, removeSpaces(value)!!.split(","))

                if (watchers.isEmpty()) {
                    event.channel.sendMessage(bot.messagesHandler.messages.taskSetWatchersNotFound).queue()
                } else {
                    task.taskData.assignees = watchers.map { it.id }

                    bot.taskManager.refreshTaskEmbed(event.channel.asThreadChannel(), task)

                    event.channel.sendMessage(bot.messagesHandler.messages.watcherSet).queue()
                }

                return

            }

            if (key == "priority") {

                val priority: TaskPriority = if (value.equals("low", true)) {
                    TaskPriority.LOW
                } else if (value.equals("normal", true)) {
                    TaskPriority.NORMAL
                } else if (value.equals("high", true)) {
                    TaskPriority.HIGH
                } else if (value.equals("urgent", true)) {
                    TaskPriority.URGENT
                } else {
                    event.channel.sendMessage(bot.messagesHandler.messages.invalidStatus).queue()
                    return
                }

                task.taskData.priority = priority.toString()
                task.priority = priority

                bot.taskManager.refreshTaskEmbed(event.channel.asThreadChannel(), task)

                event.channel.sendMessage(bot.messagesHandler.messages.prioritySet).queue()

                return

            }

            if (key == "head") {

                val roles = Utils.getRoles(event.guild!!, removeSpaces(value)!!.split(","))

                if (roles.isEmpty()) {
                    event.channel.sendMessage(bot.messagesHandler.messages.invalidHeadRole)
                    return
                }

                val taskChannel = bot.taskManager.getTaskChannel(task) ?: run {
                    event.channel.sendMessage(bot.messagesHandler.messages.taskChannelNotFound).queue()
                    return
                }

                taskChannel.headRoleIds = roles.map { it.id }

                event.channel.sendMessage(bot.messagesHandler.messages.taskChannelHeadsSet).queue()

                return

            }

        }

        if (event.subcommandName == "refreshtags") {

            if (!bot.permissionManager.hasPermission(event.member!!,com.zorbeytorunoglu.multiBot.permissions.Permission.TASK_REFRESH_TAGS)) {
                event.reply(bot.messagesHandler.messages.noPermission).setEphemeral(true).queue()
                return
            }

            val channel = bot.taskManager.getTaskChannel(event.getOption("channel")!!.asChannel) ?: run {
                event.channel.sendMessage(bot.messagesHandler.messages.taskChannelNotFound).queue()
                return
            }

            runBlocking {
                bot.taskManager.refreshTags(bot,event.guild!!,channel).await()
                event.channel.sendMessage(bot.messagesHandler.messages.refreshedTags).queue()
            }

        }

        if (event.subcommandName == "setup") {

            if (!bot.permissionManager.hasPermission(event.member!!, com.zorbeytorunoglu.multiBot.permissions.Permission.TASK_SETUP)) {
                event.reply(bot.messagesHandler.messages.noPermission).setEphemeral(true).queue()
                return
            }

            val category = event.guild!!.getCategoryById(event.getOption("category")!!.asString) ?: run {
                event.reply(bot.messagesHandler.messages.categoryNotFound).setEphemeral(true).queue()
                return
            }

            val roles = event.getOption("roles")!!.asString.split(",")

            val rolesList = roles.mapNotNull {
                Utils.getRole(event.guild!!, removeSpaces(it)!!)
            }

            if (rolesList.isEmpty()) {
                event.reply(bot.messagesHandler.messages.noRoleSpecified).setEphemeral(true).queue()
                return
            }

            val headRoles = event.getOption("heads")!!.asString.split(",")

            val headRolesList = headRoles.mapNotNull {
                Utils.getRole(event.guild!!, removeSpaces(it)!!)
            }

            if (headRolesList.isEmpty()) {
                event.reply(bot.messagesHandler.messages.invalidHeadRole).setEphemeral(true).queue()
                return
            }

            if (bot.taskManager.taskForumExists(category)) {
                event.reply(bot.messagesHandler.messages.taskChannelExists).setEphemeral(true).queue()
                return
            }

            val action = category.createForumChannel(bot.settingsHandler.settings.tasksForumChannelName)

            action.setTopic("taskChannel:${rolesList.joinToString(",") { it.id }}:" +
                    headRolesList.joinToString(",") { it.id })

            action.addPermissionOverride(event.guild!!.publicRole, mutableListOf(), mutableListOf(Permission.VIEW_CHANNEL))

            rolesList.forEach {
                action.addPermissionOverride(it, mutableListOf(
                    Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY,
                    Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_SEND,
                    Permission.MESSAGE_SEND_IN_THREADS
                ), mutableListOf(
                    Permission.ADMINISTRATOR,Permission.KICK_MEMBERS,
                    Permission.CREATE_PRIVATE_THREADS, Permission.CREATE_PUBLIC_THREADS,
                    Permission.MANAGE_THREADS, Permission.BAN_MEMBERS,
                    Permission.CREATE_INSTANT_INVITE, Permission.MANAGE_CHANNEL,
                    Permission.MANAGE_PERMISSIONS, Permission.MANAGE_EVENTS,
                    Permission.MANAGE_ROLES, Permission.MANAGE_GUILD_EXPRESSIONS,
                    Permission.MANAGE_SERVER, Permission.MANAGE_WEBHOOKS, Permission.MESSAGE_MANAGE,
                    Permission.NICKNAME_MANAGE)
                )
            }

            action.queue { forumChannel ->

                val tags: MutableList<ForumTagData> = mutableListOf()

                tags.addAll(bot.taskManager.defaultTagsData)

                val nicknameInTags = bot.settingsHandler.settings.nicknamesInTags

                rolesList.forEach {role ->

                    forumChannel.guild.getMembersWithRoles(role).forEach {member ->

                        val name = if (nicknameInTags && member.nickname != null) member.nickname!! else
                            member.effectiveName

                        if (!tags.any { it.name == name })
                            tags.add(ForumTagData(member.id).setName(name))

                    }

                }

                forumChannel.manager.setAvailableTags(tags).queue {
                    event.reply(bot.messagesHandler.messages.taskChannelCreated
                        .replace("%channel%", forumChannel.asMention)).queue()
                    val taskChannel = TaskChannel(forumChannel.id, rolesList.map { it.id }, headRolesList.map { it.id })
                    bot.taskManager.taskChannels.add(taskChannel)
                }

            }

        }

        if (event.subcommandName == "create") {

            val channel = bot.taskManager.getTaskChannel(event.getOption("channel")!!.asChannel)

            if (channel == null) {
                event.reply(bot.messagesHandler.messages.invalidChannel).setEphemeral(true).queue()
                return
            }

            if (!bot.taskManager.isHead(channel, event.member!!)) {
                event.reply(bot.messagesHandler.messages.notHead).queue()
                return
            }

            val title = event.getOption("title")!!.asString

            val description = event.getOption("description")!!.asString

            val assigneesString = event.getOption("assignees")?.asString?.split(",")

            var assignees = listOf<Member>()

            if (assigneesString != null) {
                try {
                    assignees = assigneesString.mapNotNull { Utils.getMember(event.guild!!, removeSpaces(it)!!) }
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    event.reply(bot.messagesHandler.messages.invalidAssignees).queue()
                    return
                }
            }

            val deadline = event.getOption("deadline")?.asString?.let { deadlineString ->
                try {
                    bot.taskManager.taskDateFormat.parse(deadlineString)
                } catch (e: ParseException) {
                    event.reply(bot.messagesHandler.messages.invalidDate).setEphemeral(true).queue()
                    null
                }
            }

            val watchersList = removeSpaces(event.getOption("watchers")?.asString?.trim())?.split(",")

            var watchers = listOf<Member>()

            if (watchersList != null)
                watchers = watchersList.mapNotNull { Utils.getMember(event.guild!!, it) }

            val priority = if (event.getOption("priority")?.asString == null) TaskPriority.NORMAL else
                TaskPriority.valueOf(event.getOption("priority")!!.asString)

            val forumPostAction = channel.getChannel(event.guild!!)!!.createForumPost(title, MessageCreateData.fromContent("null"))

            forumPostAction.queue { threadChannel ->

                val taskData = TaskData(threadChannel.threadChannel.id, event.member!!.id, if (assignees.isEmpty()) null else assignees.map { it.id },
                    if (watchers.isEmpty()) null else watchers.map { it.id },
                    if (deadline == null) null else bot.taskManager.taskDateFormat.format(deadline),
                    TaskStatus.OPEN.toString(), priority.toString(), null)

                val task = Task(bot, taskData)

                val editMessage = threadChannel.message.editMessage(MessageEditData.fromContent(""))

                val buttons = listOf(Button.of(ButtonStyle.SECONDARY, "open",
                    bot.settingsHandler.settings.openTag, Emoji.fromFormatted(bot.settingsHandler.settings.openTagEmoji)),
                    Button.of(ButtonStyle.PRIMARY, "in-progress",
                    bot.settingsHandler.settings.inProgressTag, Emoji.fromFormatted(bot.settingsHandler.settings.inProgressTagEmoji)),
                    Button.of(ButtonStyle.SUCCESS, "done",
                        bot.settingsHandler.settings.doneTag, Emoji.fromFormatted(bot.settingsHandler.settings.doneTagEmoji))
                    )

                val edit = MessageCreateBuilder().setEmbeds(bot.taskManager.generateTaskEmbed(title,description,task)
                    .build()).setActionRow(buttons).build()

                val tags = mutableListOf<ForumTagSnowflake>()

                bot.taskManager.getTag(channel.getChannel(event.guild!!)!!,TaskStatus.OPEN)?.let { tags.add(ForumTagSnowflake.fromId(it.id)) }

                tags.addAll(bot.taskManager.getAssigneesAsTags(
                    bot.settingsHandler.settings.nicknamesInTags, channel.getChannel(event.guild!!)!!, assignees
                ))

                editMessage.applyCreateData(edit).queue {
                    threadChannel.threadChannel.manager.setAppliedTags(tags).queue {
                        event.reply(bot.messagesHandler.messages.taskCreated.replace("%channel%",threadChannel.threadChannel.asMention)).queue()
                        channel.tasks.add(task)
                        if (bot.settingsHandler.settings.dmAssignees) {

                            if (assignees.isNotEmpty()) {
                                assignees.forEach { member ->
                                    member.user.openPrivateChannel().flatMap { it.sendMessage(
                                        bot.messagesHandler.messages.assigneeNewTaskDm
                                            .replace("%channel%", threadChannel.threadChannel.asMention)
                                    ) }.queue()
                                }
                            }

                        }
                    }
                }

            }

        }

        if (event.subcommandName == "delete") {

            if (!bot.permissionManager.hasPermission(event.member!!,com.zorbeytorunoglu.multiBot.permissions.Permission.TASK_DELETE)) {
                event.reply(bot.messagesHandler.messages.noPermission).setEphemeral(true).queue()
                return
            }

            val task = bot.taskManager.getTask(bot, event.channel) ?: run {
                event.channel.sendMessage(bot.messagesHandler.messages.taskNotFound).queue()
                return
            }

            val taskChannel = bot.taskManager.getTaskChannel(task) ?: run {
                event.channel.sendMessage(bot.messagesHandler.messages.taskNotFound).queue()
                return
            }

            taskChannel.tasks.remove(task)

            event.channel.delete().queue()

        }

    }

    private fun removeSpaces(input: String?): String? {
        return input?.replace("\\s".toRegex(), "")
    }

    private fun getTaskCountMessage(mentionable: String, count: Int, status: String): String {
        return bot.messagesHandler.messages.taskCountStatusMentionable
            .replace("%mentionable%", mentionable)
            .replace("%status%", status)
            .replace("%count%", "$count")
    }

}