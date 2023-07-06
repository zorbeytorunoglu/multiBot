package com.zorbeytorunoglu.multiBot.messages

data class Messages(

    val ticketPanelSent: String = "Ticket panel is sent!",
    val noPermission: String = "You have no permission!",
    val ticketPanelIdOption: String = "The ID of the ticket panel (ticketPanels.json)",
    val targetCategoryNotFound: String = "Target category could not be found.",
    val ticketReady: String = "Your ticket is ready! %ticket%",
    val exceedTicketLimit: String = "You can not exceed your ticket limit!",
    val reminder: String = "**Reminder!**",
    val reminderSet: String = "Reminder set!",
    val notAudioChannel: String = "That channel is not an audio channel!",
    val alreadyConnected: String = "Bot is already connected to a channel. Try '/record stop' first.",
    val recordingStarted: String = "Recording started!",
    val notRecording: String = "Bot is not recording!",
    val recordingStopped: String = "Recording is stopped!",
    val notInVoiceChannel: String = "You are not in a voice channel!"

)
