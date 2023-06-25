package com.zorbeytorunoglu.multiBot.commands.configuration

data class Commands(
    val pingCmd: String = "ping",
    val pingDesc: String = "Shows the ping of the bot.",
    val ticketPanelCmd: String = "ticketpanel",
    val ticketPanelDesc: String = "Sends a ticket panel to the channel."
)
