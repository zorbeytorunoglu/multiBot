package com.zorbeytorunoglu.multiBot.settings

import com.zorbeytorunoglu.multiBot.utils.GsonUtils
import java.io.File

class SettingsHandler {

    val settings: Settings

    init {
        settings = loadSettings()
    }

    private fun loadSettings(): Settings {

        val file = File("settings.json")

        return GsonUtils.loadFromJson(file, Settings::class.java)

    }

}