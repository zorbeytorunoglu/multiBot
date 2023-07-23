package com.zorbeytorunoglu.multiBot.commands.task

import com.zorbeytorunoglu.multiBot.Bot
import com.zorbeytorunoglu.multiBot.commands.Command
import com.zorbeytorunoglu.multiBot.task.*
import com.zorbeytorunoglu.multiBot.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.forums.ForumTagData
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
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

    // /task setup <category> <roles>

    override fun subcommandData(): Collection<SubcommandData> {

        val priorityChoices = mutableListOf<Choice>()

        TaskPriority.values().forEach {
            priorityChoices.add(Choice(it.name, it.name))
        }

        return listOf(
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
                )
        )
    }

    override fun execute(event: SlashCommandInteractionEvent) {

        if (event.subcommandName == "setup") {

            val category = event.guild!!.getCategoryById(event.getOption("category")!!.asString) ?: run {
                event.reply(bot.messagesHandler.messages.categoryNotFound).setEphemeral(true).queue()
                return
            }

            val roles = event.getOption("roles")!!.asString.split(",")

            val rolesList = roles.mapNotNull {
                Utils.getRole(event.guild!!, removeSpaces(it))
            }

            if (rolesList.isEmpty()) {
                event.reply(bot.messagesHandler.messages.noRoleSpecified).setEphemeral(true).queue()
                return
            }

            val headRoles = event.getOption("heads")!!.asString.split(",")

            val headRolesList = headRoles.mapNotNull {
                Utils.getRole(event.guild!!, removeSpaces(it))
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

            action.setTopic(bot.settingsHandler.settings.taskForumChannelTopic)

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

        // /task create <channel> <title> <description> <assignee> <deadline> <watchers>

        if (event.subcommandName == "create") {

            val channel = bot.taskManager.getTaskChannel(event.getOption("channel")!!.asChannel)

            if (channel == null) {
                event.reply(bot.messagesHandler.messages.invalidChannel).setEphemeral(true).queue()
                return
            }

            val title = event.getOption("title")!!.asString

            val description = event.getOption("description")!!.asString

            val assigneesString = event.getOption("assignees")?.asString?.split(",")

            var assignees = listOf<Member>()

            if (assigneesString != null) {
               assignees = assigneesString.mapNotNull { Utils.getMember(event.guild!!, removeSpaces(it)) }
            }

            val deadline = event.getOption("deadline")?.asString?.let { deadlineString ->
                try {
                    bot.taskManager.taskDateFormat.parse(deadlineString)
                } catch (e: ParseException) {
                    event.reply(bot.messagesHandler.messages.invalidDate).setEphemeral(true).queue()
                    null
                }
            }

            val watchersList = event.getOption("watchers")?.asString?.split(",")

            var watchers = listOf<Member>()

            if (watchersList != null)
                watchers = watchersList.mapNotNull { Utils.getMember(event.guild!!, it) }

            val priority = if (event.getOption("priority")?.asString == null) TaskPriority.NORMAL else
                TaskPriority.valueOf(event.getOption("priority")!!.asString)

            val forumPostAction = channel.getChannel(event.guild!!)!!.createForumPost(title, MessageCreateData.fromContent("null"))

            CoroutineScope(Dispatchers.Default).launch {
                bot.taskManager.refreshTags(bot,event.guild!!,channel).await()
                forumPostAction.setTags(bot.taskManager.getAssigneesAsTags(
                    bot.settingsHandler.settings.nicknamesInTags,forumPostAction.channel,assignees
                ))
            }

            forumPostAction.queue { threadChannel ->

                val taskData = TaskData(threadChannel.threadChannel.id, event.member!!.id, if (assignees.isEmpty()) null else assignees.map { it.id },
                    if (watchers.isEmpty()) null else watchers.map { it.id },
                    if (deadline == null) null else bot.taskManager.taskDateFormat.format(deadline),
                    TaskStatus.OPEN.toString(), priority.toString(), channel.roles)

                val task = Task(bot, taskData)

                val editMessage = threadChannel.message.editMessage(MessageEditData.fromContent(""))

                val edit = MessageCreateBuilder().setEmbeds(bot.taskManager.generateTaskEmbed(title,description,task).build()).build()

                val tags = mutableListOf<ForumTagSnowflake>()

                bot.taskManager.getTag(channel.getChannel(event.guild!!)!!,TaskStatus.OPEN)?.let { tags.add(ForumTagSnowflake.fromId(it.id)) }

                tags.addAll(bot.taskManager.getAssigneesAsTags(
                    bot.settingsHandler.settings.nicknamesInTags, channel.getChannel(event.guild!!)!!, assignees
                ))

                editMessage.applyCreateData(edit).queue {
                    threadChannel.threadChannel.manager.setAppliedTags(tags).queue {
                        event.reply(bot.messagesHandler.messages.taskCreated.replace("%channel%",threadChannel.threadChannel.asMention)).queue()
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

    }

    private fun removeSpaces(input: String): String {
        return input.replace("\\s".toRegex(), "")
    }

}