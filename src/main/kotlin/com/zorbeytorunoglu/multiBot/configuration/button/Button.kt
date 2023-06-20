package com.zorbeytorunoglu.multiBot.configuration.button

import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

class Button(buttonConfig: ButtonConfig) {

    val id: String
    val button: Button

    init {
        id = buttonConfig.id
        button = Button.of(ButtonStyle.valueOf(buttonConfig.style),
            id, buttonConfig.label, if (buttonConfig.emoji == null) null
        else Emoji.fromUnicode(buttonConfig.emoji))
    }

}