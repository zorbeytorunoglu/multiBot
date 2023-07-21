package com.zorbeytorunoglu.multiBot.task

data class TaskData(
    val taskId: String,
    val assignees: Collection<String>,
    val watchers: Collection<String>,
    val deadline: String,
    val status: String,
    val priority: String,
    val departmentRole: String
)