package com.zorbeytorunoglu.multiBot

import com.zorbeytorunoglu.multiBot.listeners.ReadyListener
import com.zorbeytorunoglu.multiBot.settings.SettingsHandler
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag

fun main() {

    val settingsHandler = SettingsHandler()

    val builder = JDABuilder.createDefault(settingsHandler.settings.token)

    val bot = Bot()

    bot.settingsHandler = settingsHandler

    builder.addEventListeners(ReadyListener(bot))

    builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)

    builder.enableCache(CacheFlag.MEMBER_OVERRIDES)

    builder.setMemberCachePolicy(MemberCachePolicy.ALL)

    bot.jda = builder.build()

    //TODO: Shutdown saves

}