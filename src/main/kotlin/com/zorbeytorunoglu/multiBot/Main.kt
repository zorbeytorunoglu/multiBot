package com.zorbeytorunoglu.multiBot

import com.zorbeytorunoglu.multiBot.events.EventManager
import com.zorbeytorunoglu.multiBot.settings.SettingsHandler
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag

fun main() {

    val settingsHandler = SettingsHandler()

    val builder = JDABuilder.createDefault(settingsHandler.settings.token)

    val bot = Bot()

    bot.settingsHandler = settingsHandler

    builder.setChunkingFilter(ChunkingFilter.ALL)

    builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)

    builder.enableCache(CacheFlag.MEMBER_OVERRIDES)

    builder.setMemberCachePolicy(MemberCachePolicy.ALL)

    builder.setEventManager(EventManager(bot).registerEvents())

    bot.jda = builder.build()

    Runtime.getRuntime().addShutdownHook(Thread {
        bot.jda.shutdown()
        bot.permissionManager.savePermissions()
    })

}