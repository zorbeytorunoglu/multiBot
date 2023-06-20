package com.zorbeytorunoglu.multiBot.ticket.configuration

import com.google.gson.GsonBuilder
import com.zorbeytorunoglu.multiBot.configuration.button.ButtonConfig
import com.zorbeytorunoglu.multiBot.configuration.embedmessage.EmbedMessageConfig
import com.zorbeytorunoglu.multiBot.ticket.TicketPanelConfig
import java.io.File

class TicketConfigurationHandler {

    val ticketPanels = HashMap<String, TicketPanel>()
    val ticketPanelConfigs = HashMap<String, TicketPanelConfig>()

    init {
        loadTicketPanels()
    }

    fun loadTicketPanels() {

        val file = File("ticketPanels.json")

        if (!file.exists()) {

            val embedMessageConfig = EmbedMessageConfig("embedBaslik", null,
                "aciklama",null,null,null,"ORANGE",null,
                null,null,null,null)

            val buttonConfig = ButtonConfig("SECONDARY","buId","labelBu",null)

            val buttonsConfig: List<TicketButtonConfig> = listOf(
                TicketButtonConfig(buttonConfig,"1203910239021")
            )

            val ticketPanelConfig = TicketPanelConfig("baslik",embedMessageConfig,
                buttonsConfig,"12031920",embedMessageConfig)

            ticketPanelConfigs["1"] = ticketPanelConfig

            val ticketPanel = TicketPanel(ticketPanelConfig)

            val gson = GsonBuilder().setPrettyPrinting().create()

            val json = gson.toJson(ticketPanelConfigs)

            println("aha json")
            println(json)

        }

        //TODO: Return ticketPanels hashmap

    }

}