package com.zorbeytorunoglu.multiBot.commands.admin

import com.zorbeytorunoglu.multiBot.Bot
import com.zorbeytorunoglu.multiBot.commands.Command
import com.zorbeytorunoglu.multiBot.permissions.HolderType
import com.zorbeytorunoglu.multiBot.permissions.Permission
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import java.lang.StringBuilder

class PermissionCommand(private val bot: Bot): Command {

    //TODO: /permission give/remove/check <member,role> <permission>

    override val name: String
        get() = bot.commandsConfigurationHandler.commands.permissionCmd
    override val description: String
        get() = bot.commandsConfigurationHandler.commands.permissionDesc
    override val guildOnly: Boolean
        get() = true

    override fun optionData(): Collection<OptionData> {
        return emptyList()
    }

    override fun subcommandData(): Collection<SubcommandData> {
        val permissionChoices: List<Choice> =
            Permission.values().map { Choice(it.name, it.name) }

        return listOf(
            SubcommandData("give", "Gives a permission.")
                .addOptions(
                    OptionData(OptionType.MENTIONABLE, "holder", "Role/member", true, false),
                    OptionData(OptionType.STRING, "permission", "Permission to give.", true, false)
                        .addChoices(permissionChoices)
                ),
            SubcommandData("remove", "Removes a permission.")
                .addOptions(
                    OptionData(OptionType.MENTIONABLE, "holder", "Role/member", true, false),
                    OptionData(OptionType.STRING, "permission", "Permission to remove.", true, false)
                        .addChoices(permissionChoices)
                ),
            SubcommandData("check", "Checks the permissions ofa role/member.")
                .addOptions(
                    OptionData(OptionType.MENTIONABLE, "holder", "Role/member", true, false)
                )
        )
    }

    override fun execute(event: SlashCommandInteractionEvent) {

        if (!bot.permissionManager.hasPermission(event.member!!, Permission.PERMISSION)) {
            event.reply(bot.messagesHandler.messages.noPermission)
                .setEphemeral(true).queue()
            return
        }

        if (event.subcommandName == null) return

        if (event.subcommandName == "give") {

            val holder = event.getOption("holder")!!

            val holderType: HolderType = if (event.guild!!.getMemberById(holder.asMentionable.id) != null) {
                HolderType.USER
            } else if (event.guild!!.getRoleById(holder.asMentionable.id) != null) {
                HolderType.GROUP
            } else {
                event.reply(bot.messagesHandler.messages.mustBeMemberOrRole)
                    .setEphemeral(true).queue()
                return
            }

            if (event.getOption("permission") == null) {
                event.reply(bot.messagesHandler.messages.notValidPermission)
                    .setEphemeral(true).queue()
                return
            }

            val permission: Permission

            try {
                permission = Permission.valueOf(event.getOption("permission")!!.asString)
            } catch (e: Exception) {
                event.reply(bot.messagesHandler.messages.notValidPermission)
                    .setEphemeral(true).queue()
                return
            }

            if (bot.permissionManager.hasPermission(holder.asMentionable.id, permission)) {
                event.reply(bot.messagesHandler.messages.alreadyHasThisPermission)
                    .setEphemeral(true).queue()
                return
            }

            bot.permissionManager.addPermission(holder.asMentionable.id, holderType, permission)

            event.reply(bot.messagesHandler.messages.permissionGiven
                .replace("%holder%", holder.asMentionable.asMention)
                .replace("%permission%", permission.toString())).setEphemeral(true)
                .queue()

        }

        else if (event.subcommandName == "remove") {

            val holder = event.getOption("holder")!!

            val holderType: HolderType = if (event.guild!!.getMemberById(holder.asMentionable.id) != null) {
                HolderType.USER
            } else if (event.guild!!.getRoleById(holder.asMentionable.id) != null) {
                HolderType.GROUP
            } else {
                event.reply(bot.messagesHandler.messages.mustBeMemberOrRole)
                    .setEphemeral(true).queue()
                return
            }

            if (event.getOption("permission") == null) {
                event.reply(bot.messagesHandler.messages.notValidPermission)
                    .setEphemeral(true).queue()
                return
            }

            val permission: Permission

            try {
                permission = Permission.valueOf(event.getOption("permission")!!.asString)
            } catch (e: Exception) {
                event.reply(bot.messagesHandler.messages.notValidPermission)
                    .setEphemeral(true).queue()
                return
            }

            if (!bot.permissionManager.hasPermission(holder.asMentionable.id, permission)) {
                event.reply(bot.messagesHandler.messages.alreadyHasNotThisPermission)
                    .setEphemeral(true).queue()
                return
            }

            bot.permissionManager.removePermission(holder.asMentionable.id, permission)

            event.reply(bot.messagesHandler.messages.permissionRemoved
                .replace("%holder%", holder.asMentionable.asMention)
                .replace("%permission%", permission.toString())).setEphemeral(true)
                .queue()

        } else if (event.subcommandName == "check") {

            val holder = event.getOption("holder")!!

            val holderType: HolderType = if (event.guild!!.getMemberById(holder.asMentionable.id) != null) {
                HolderType.USER
            } else if (event.guild!!.getRoleById(holder.asMentionable.id) != null) {
                HolderType.GROUP
            } else {
                event.reply(bot.messagesHandler.messages.mustBeMemberOrRole)
                    .setEphemeral(true).queue()
                return
            }

            val permList = bot.permissionManager.permissionListOf(holder.asMentionable)

            if (permList.isEmpty()) {
                event.reply(bot.messagesHandler.messages.hasNoPermission)
                    .setEphemeral(true).queue()
                return
            }

            val builder = StringBuilder()

            builder.append("**${bot.messagesHandler.messages.permissionCheckTitle}**"
                .replace("%holder%", holder.asMentionable.asMention)).append("\n").append("\n")

            permList.forEach {
                if (permList.indexOf(it) == permList.lastIndex) {
                    builder.append(it.name)
                } else {
                    builder.append("${it.name}, ")
                }
            }

            val embedB = EmbedBuilder()

            embedB.setDescription(builder.toString())

            event.replyEmbeds(embedB.build()).setEphemeral(true).queue()

        } else return

    }

}