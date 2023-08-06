package com.zorbeytorunoglu.multiBot.events

import com.zorbeytorunoglu.multiBot.Bot
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class AbstractListener(protected val bot: Bot): SuspendListener() {

    val logger: Logger = LoggerFactory.getLogger(AbstractListener::class.java.name)

}