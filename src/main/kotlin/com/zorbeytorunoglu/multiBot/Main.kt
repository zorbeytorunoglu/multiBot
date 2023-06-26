package com.zorbeytorunoglu.multiBot

import com.zorbeytorunoglu.multiBot.listeners.ReadyListener
import com.zorbeytorunoglu.multiBot.settings.SettingsHandler
import net.dv8tion.jda.api.JDABuilder

fun main() {

    val settingsHandler = SettingsHandler()

    val builder = JDABuilder.createDefault(settingsHandler.settings.token)

    val bot = Bot()

    bot.settingsHandler = settingsHandler

    builder.addEventListeners(ReadyListener(bot))

    bot.jda = builder.build()

}