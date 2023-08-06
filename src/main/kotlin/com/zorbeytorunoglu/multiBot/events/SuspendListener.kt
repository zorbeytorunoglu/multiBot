package com.zorbeytorunoglu.multiBot.events

import net.dv8tion.jda.api.events.GenericEvent

abstract class SuspendListener {

    abstract suspend fun onEvent(event: GenericEvent)

}