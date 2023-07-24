package com.zorbeytorunoglu.multiBot.settings

data class Settings(

    val token: String = " ",
    val status: String = "Multi Bot",
    val activity: String = "watching",
    val activityLabel: String = "over you",
    val disabledCaches: List<String> = listOf("VOICE_STATE"),
    val enabledCaches: List<String> = listOf("MEMBER_OVERRIDES"),
    val disabledIntents: List<String> = listOf("GUILD_MESSAGE_TYPING"),
    val enabledIntents: List<String> = listOf("GUILD_MEMBERS"),
    val tasksForumChannelName: String = "tasks",
    val taskDateFormat: String = "dd-MM-yyyy",
    val inProgressTag: String = "In Progress",
    val inProgressTagEmoji: String = "<:golub:1122534002599927920>",
    val openTag: String = "Open",
    val openTagEmoji: String = "<:golub:1122534002599927920>",
    val doneTag: String = "Done",
    val doneTagEmoji: String = "<:golub:1122534002599927920>",
    val nicknamesInTags: Boolean = false,
    val dmAssignees: Boolean = true

)
