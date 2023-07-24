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
    val notInVoiceChannel: String = "You are not in a voice channel!",
    val mustBeMemberOrRole: String = "Holder must be a member or a role!",
    val notValidPermission: String = "It is not a valid permission!",
    val permissionGiven: String = "Permission %permission% is given to %holder%!",
    val alreadyHasThisPermission: String = "Subject already has this permission!",
    val alreadyHasNotThisPermission: String = "Subject does not have this permission already!",
    val permissionRemoved: String = "Permission %permission% is removed from %holder%!",
    val hasNoPermission: String = "Subject does not have any permission. If he can use the commands, he may be authorized by the guild.",
    val permissionCheckTitle: String = "Permission List of %holder%",
    val saySent: String = "Message is sent!",
    val memberNotFound: String = "Member could not be found.",
    val notATicket: String = "This is not a ticket channel!",
    val alreadyInTicket: String = "Member is already has permission to view and send message to the ticket!",
    val addedToTicket: String = "%member% has been added to the ticket!",
    val notInTicket: String = "Member is not in the ticket!",
    val ticketWillBeDeleted: String = "Ticket will be in 10 seconds!",
    val deleteConfirm: String = "Submit /delete command again to confirm to delete.",
    val transcriptRetrievingMessages: String = "Retrieving messages...",
    val transcriptMessagesRetrieved: String = "Messages are retrieved, creating the transcript...",
    val transcriptGenerating: String = "Generating a transcript...",
    val transcriptGenerated: String = "Transcript is generated!",
    val categoryNotFound: String = "Category with that ID could not be found.",
    val noRoleSpecified: String = "No role is specified.",
    val taskChannelExists: String = "There is already a task channel in that category.",
    val taskChannelCreated: String = "Task channel %channel% has been created!",
    val invalidHeadRole: String = "Invalid head role!",
    val invalidChannel: String = "Invalid channel!",
    val invalidDate: String = "Invalid date!",
    val taskCreated: String = "Task is created! Here it is: %channel%",
    val assigneeNewTaskDm: String = "You have a new task! %channel%",
    val taskEmbedDeadline: String = "Deadline",
    val taskEmbedAssignees: String = "Assignees",
    val taskEmbedGivenBy: String = "Given By",
    val taskEmbedWatchers: String = "Watchers",
    val taskEmbedPriority: String = "Priority",
    val taskEmbedStatus: String = "Status"

)
