package com.zorbeytorunoglu.multiBot.messages

data class Messages(

    val ticketPanelSent: String = "Ticket panel is sent!",
    val noPermission: String = "You have no permission!",
    val ticketPanelIdOption: String = "The ID of the ticket panel (ticketPanels.json)",
    val targetCategoryNotFound: String = "Target category could not be found.",
    val ticketReady: String = "Your ticket is ready! %ticket%",
    val exceedTicketLimit: String = "You can not exceed your ticket limit!"

)
