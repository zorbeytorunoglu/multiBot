package com.zorbeytorunoglu.multiBot.settings

data class Settings(
    val token: String = " ",
    val status: String = "Multi Bot",
    val activity: String = "watching",
    val activityLabel: String = "over you",
    val disabledCaches: List<String> = listOf("VOICE_STATE"),
    val enabledCaches: List<String> = listOf("MEMBER_OVERRIDES"),
    val disabledIntents: List<String> = listOf("GUILD_MESSAGE_TYPING"),
    val enabledIntents: List<String> = listOf("GUILD_MEMBERS")
)
