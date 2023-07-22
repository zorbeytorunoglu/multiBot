package com.zorbeytorunoglu.multiBot.task

import com.zorbeytorunoglu.multiBot.utils.Utils
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel

class TaskChannel(val channelId: String, val roles: Collection<String>, val headRoleIds: Collection<String>) {

    val tasks: ArrayList<Task> = ArrayList()

    fun getHeadRoles(guild: Guild): List<Role> {

        return headRoleIds.mapNotNull {
            Utils.getRole(guild, it)
        }

    }

    fun getChannel(guild: Guild): ForumChannel? {

        return guild.getForumChannelById(channelId)

    }

    fun getRoles(guild: Guild): List<Role> {

        return roles.mapNotNull { guild.getRoleById(it) }

    }

}