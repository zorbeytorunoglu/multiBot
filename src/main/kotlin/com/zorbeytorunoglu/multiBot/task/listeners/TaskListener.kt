package com.zorbeytorunoglu.multiBot.task.listeners

import com.zorbeytorunoglu.multiBot.Bot
import com.zorbeytorunoglu.multiBot.task.TaskStatus
import com.zorbeytorunoglu.multiBot.utils.Utils
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class TaskListener(private val bot: Bot): ListenerAdapter() {

    override fun onChannelDelete(event: ChannelDeleteEvent) {

        if (event.channel.type == ChannelType.FORUM) {
            val taskChannel = bot.taskManager.taskChannels.find { it.channelId == event.channel.id }
            if (taskChannel != null) bot.taskManager.taskChannels.remove(taskChannel)
        }

        if (event.channel.type == ChannelType.GUILD_PUBLIC_THREAD || event.channel.type == ChannelType.GUILD_PRIVATE_THREAD) {

            val task = bot.taskManager.getTask(event.channel) ?: return

            bot.taskManager.getTaskChannel(task)!!.tasks.remove(task)

        }

    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {

        if (event.channel.type != ChannelType.GUILD_PUBLIC_THREAD && event.channel.type != ChannelType.GUILD_PRIVATE_THREAD) return

        if (!bot.taskManager.isTask(event.channel.asThreadChannel())) return

        if (event.message.author.id != bot.jda.selfUser.id) return

        val task = bot.taskManager.getTask(bot, event.channel) ?: run {
            event.reply(bot.messagesHandler.messages.taskNotFound).queue()
            return
        }

        val member = event.member!!

        if (!Utils.isAdmin(member) && !bot.taskManager.isAssignee(member, task) &&
            !bot.taskManager.isWatcher(member, task)) {
            event.reply(bot.messagesHandler.messages.noPermission).queue()
            return
        }

        val status = task.status

        when (event.button.id) {

            "open" -> {
                if (status == TaskStatus.OPEN) {
                    event.reply(bot.messagesHandler.messages.statusAlready
                        .replace("%status%", bot.settingsHandler.settings.openTag)).queue()
                    return
                } else {
                    bot.taskManager.updateStatus(task, TaskStatus.OPEN)
                    event.reply(bot.messagesHandler.messages.statusUpdated
                        .replace("%status%", bot.settingsHandler.settings.openTag)
                        .replace("%member%", member.asMention)).queue()
                    return
                }
            }

            "done" -> {

                if (status == TaskStatus.DONE) {
                    event.reply(bot.messagesHandler.messages.statusAlready
                        .replace("%status%", bot.settingsHandler.settings.doneTag)).queue()
                    return
                } else {
                    event.reply(bot.messagesHandler.messages.statusUpdated
                        .replace("%status%", bot.settingsHandler.settings.doneTag)
                        .replace("%member%", member.asMention)).queue {

                            if (bot.settingsHandler.settings.notifyWatchersOnTaskDone) {
                                task.getWatchers().forEach {
                                    Utils.sendDirectMessage(it.user,
                                        bot.messagesHandler.messages.watcherTaskDoneNotification
                                            .replace("%channel%", event.channel.asMention))
                                }
                            }

                        bot.taskManager.markComplete(task)

                    }

                    return
                }

            }

            "in-progress" -> {

                if (status == TaskStatus.IN_PROGRESS) {
                    event.reply(bot.messagesHandler.messages.statusAlready
                        .replace("%status%", bot.settingsHandler.settings.inProgressTag)).queue()
                    return
                } else {
                    bot.taskManager.updateStatus(task, TaskStatus.IN_PROGRESS)
                    event.reply(bot.messagesHandler.messages.statusUpdated
                        .replace("%status%", bot.settingsHandler.settings.inProgressTag)
                        .replace("%member%", member.asMention)).queue()
                    return
                }

            }

            else -> return

        }

    }

}