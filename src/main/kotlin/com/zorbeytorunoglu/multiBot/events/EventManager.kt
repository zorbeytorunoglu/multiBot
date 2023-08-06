package com.zorbeytorunoglu.multiBot.events

import com.zorbeytorunoglu.multiBot.Bot
import com.zorbeytorunoglu.multiBot.events.listeners.ReadyListener
import com.zorbeytorunoglu.multiBot.events.listeners.SlashCommandListener
import com.zorbeytorunoglu.multiBot.events.listeners.TaskListener
import com.zorbeytorunoglu.multiBot.events.listeners.TicketListener
import com.zorbeytorunoglu.multiBot.threading.TaskManager
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.hooks.IEventManager

class EventManager(private val bot: Bot): IEventManager {

    private val listeners = ArrayList<SuspendListener>()

    fun registerEvents(): EventManager {

        listeners.add(TaskListener(bot))
        listeners.add(ReadyListener(bot))
        listeners.add(SlashCommandListener(bot))
        listeners.add(TicketListener(bot))

        return this

    }

    override fun register(listener: Any) {
        throw IllegalArgumentException()
    }

    override fun unregister(listener: Any) {
        throw IllegalArgumentException()
    }

    override fun handle(event: GenericEvent) {

        TaskManager.async {

            try {
                listeners.forEach { it.onEvent(event) }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }

    override fun getRegisteredListeners(): MutableList<Any> {
        return listeners.toMutableList()
    }


}