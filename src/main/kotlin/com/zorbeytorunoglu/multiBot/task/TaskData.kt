package com.zorbeytorunoglu.multiBot.task

data class TaskData(
    val taskId: String,
    val givenBy: String,
    var assignees: Collection<String>?,
    var watchers: Collection<String>?,
    var deadline: String?,
    var status: String,
    var priority: String
)