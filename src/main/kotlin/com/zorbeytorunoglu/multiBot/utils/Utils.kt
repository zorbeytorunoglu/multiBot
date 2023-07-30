package com.zorbeytorunoglu.multiBot.utils

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Mentions
import net.dv8tion.jda.api.entities.Role
import java.awt.Color

object Utils {

    fun getColor(color: String): Color {
        return when (color) {
            "YELLOW" -> Color.YELLOW
            "BLUE" -> Color.BLUE
            "BLACK" -> Color.BLACK
            "RED" -> Color.RED
            "PINK" -> Color.PINK
            "CYAN" -> Color.CYAN
            "GRAY" -> Color.GRAY
            "DARK_GREY" -> Color.DARK_GRAY
            "GREEN" -> Color.GREEN
            "MAGENTA" -> Color.MAGENTA
            "WHITE" -> Color.WHITE
            "LIGHT_GRAY" -> Color.LIGHT_GRAY
            else -> Color.ORANGE
        }
    }

    fun getRole(guild: Guild, string: String): Role? {
        val roleId = string.takeIf { it.startsWith("<@&") && it.endsWith(">") }?.substring(3, string.length - 1)
        return roleId?.let { guild.getRoleById(it) } ?: guild.getRolesByName(string, true).firstOrNull() ?: guild.getRoleById(string)
    }

    fun getMember(guild: Guild, string: String): Member? {
        val memberId = string.takeIf { it.startsWith("<@") && it.endsWith(">") }?.substring(2, string.length - 1)
        return memberId?.let { guild.getMemberById(it) } ?: guild.getMembersByName(string, true).firstOrNull() ?: guild.getMemberById(string)
    }

    fun getMembers(guild: Guild, members: List<String>): List<Member> {
        return members.mapNotNull { getMember(guild, it) }
    }

    fun getRoles(guild: Guild, roles: List<String>): List<Role> {
        return roles.mapNotNull { getRole(guild, it) }
    }

    fun isRole(mention: Mentions): Boolean {
        return mention.roles.isNotEmpty()
    }

}