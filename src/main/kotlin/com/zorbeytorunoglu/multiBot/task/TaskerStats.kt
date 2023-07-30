package com.zorbeytorunoglu.multiBot.task

data class TaskerStats(val id: String) {

    var completedTasks: MutableList<String> = mutableListOf()
    var completedOnTime: MutableList<String> = mutableListOf()
    var completedAfterDeadline: MutableList<String> = mutableListOf()
    var activeTasks: MutableList<String> = mutableListOf()
    var inProgressTasks: MutableList<String> = mutableListOf()

}