package com.zorbeytorunoglu.multiBot.utils

import java.awt.Color

object Utils {

    fun getColor(color: String): Color {
        return when (color) {
            "YELLOW" -> Color.YELLOW
            "BLUE" -> Color.BLUE
            "BLACK" -> Color.BLACK
            "RED" -> Color.RED
            "PINK" -> Color.PINK
            "CYAN" -> Color.CYAN
            "GRAY" -> Color.GRAY
            "DARK_GREY" -> Color.DARK_GRAY
            "GREEN" -> Color.GREEN
            "MAGENTA" -> Color.MAGENTA
            "WHITE" -> Color.WHITE
            "LIGHT_GRAY" -> Color.LIGHT_GRAY
            else -> Color.ORANGE
        }
    }

}