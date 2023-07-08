package com.zorbeytorunoglu.multiBot.commands.configuration

data class Commands(
    val pingCmd: String = "ping",
    val pingDesc: String = "Shows the ping of the bot.",
    val ticketPanelCmd: String = "ticketpanel",
    val ticketPanelDesc: String = "Sends a ticket panel to the channel.",
    val kickCmd: String = "kick",
    val kickDesc: String = "Kicks a member.",
    val banCmd: String = "ban",
    val banDesc: String = "Bans a member.",
    val remindCmd: String = "remind",
    val remindDesc: String = "Schedules a reminder in that channel.",
    val recordCmd: String = "record",
    val recordDesc: String = "Records the voices in a voice channel.",
    val permissionCmd: String = "permission",
    val permissionDesc: String = "Permission management command.",
    val sayCmd: String = "say",
    val sayDesc: String = "Makes the bot send the message provided.",
    val addCmd: String = "add",
    val addDesc: String = "Adds a member to the ticket.",
    val ticketCmd: String = "ticket",
    val ticketDesc: String = "Ticket moderation command."
)
