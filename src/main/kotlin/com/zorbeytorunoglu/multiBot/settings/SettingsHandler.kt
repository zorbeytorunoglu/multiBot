package com.zorbeytorunoglu.multiBot.settings

import com.zorbeytorunoglu.multiBot.utils.FileUtils
import java.io.File

class SettingsHandler {

    val settings: Settings

    init {
        settings = loadSettings()
    }

    private fun loadSettings(): Settings {

        val file = File("settings.json")

        return FileUtils.loadFromJson(file, Settings::class.java)

    }

}