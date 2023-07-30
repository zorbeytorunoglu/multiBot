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
    val inProgressTagEmoji: String = "<:task_in_progress:1134968344181735484>",
    val openTag: String = "Open",
    val openTagEmoji: String = "<:task_folder:1134968339429593180>",
    val doneTag: String = "Done",
    val doneTagEmoji: String = "<:task_done:1134968342319464478>",
    val nicknamesInTags: Boolean = false,
    val dmAssignees: Boolean = true,
    val scheduledDeadlineCheck: Boolean = true,
    val deadlineCheckDelayHour: Int = 24,
    val notifyAssigneesPassedDeadline: Boolean = true,
    val notifyWatchersPassedDeadline: Boolean = true

)
