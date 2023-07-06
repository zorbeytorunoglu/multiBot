package com.zorbeytorunoglu.multiBot.listeners

import com.zorbeytorunoglu.multiBot.Bot
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.IEventManager

class EventManager(private val bot: Bot): IEventManager {

    override fun register(listener: Any) {
        throw IllegalArgumentException()
    }

    override fun unregister(listener: Any) {
        throw IllegalArgumentException()
    }

    override fun handle(event: GenericEvent) {

        val event = event as MessageReceivedEvent

        event.guild.audioManager.receivingHandler

    }

    override fun getRegisteredListeners(): MutableList<Any> {
        TODO("Not yet implemented")
    }


}