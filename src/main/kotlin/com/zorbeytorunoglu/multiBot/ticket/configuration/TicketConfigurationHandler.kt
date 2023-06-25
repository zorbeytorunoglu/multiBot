package com.zorbeytorunoglu.multiBot.ticket.configuration

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.zorbeytorunoglu.multiBot.configuration.button.ButtonConfig
import com.zorbeytorunoglu.multiBot.configuration.embedmessage.EmbedMessageConfig
import com.zorbeytorunoglu.multiBot.utils.FileUtils
import java.io.File

class TicketConfigurationHandler {

    val ticketPanels = HashMap<String, TicketPanel>()

    init {
        loadTicketPanels()
    }

    private fun loadTicketPanels() {

        val file = File("ticketPanels.json")

        var ticketPanelConfigs = HashMap<String, TicketPanelConfig>()

        if (!file.exists()) {

            file.createNewFile()

            val embedMessageConfig = EmbedMessageConfig("example", null,
                "description","example",null,null,
                "ORANGE","example",
                null,null,null,null)

            val buttonConfig = ButtonConfig("SECONDARY","example","example",null)

            val buttonsConfig: List<TicketButtonConfig> = listOf(
                TicketButtonConfig(buttonConfig,"1203910239021")
            )

            val ticketPanelConfig = TicketPanelConfig("title-",embedMessageConfig,
                buttonsConfig,"12031920",embedMessageConfig)

            ticketPanelConfigs["1"] = ticketPanelConfig

            val ticketPanel = TicketPanel(ticketPanelConfig)

            ticketPanels["1"] = ticketPanel

            val gson = GsonBuilder().setPrettyPrinting().create()

            val json = gson.toJson(ticketPanelConfigs)

            FileUtils.writeJsonToFile(json, file)

        } else {

            val json = FileUtils.readFile(file)

            if (json == null) {
                println("File ${file.name} could not be read due to an error.")
                return
            }

            val type = object : TypeToken<HashMap<String, TicketPanelConfig>>() {}.type

            val gson = GsonBuilder().setPrettyPrinting().create()

            ticketPanelConfigs = gson.fromJson(json, type)

        }

        if (ticketPanelConfigs.isNotEmpty()) {

            ticketPanelConfigs.keys.forEach {
                ticketPanels[it] = TicketPanel(ticketPanelConfigs[it]!!)
            }

        }

    }

}