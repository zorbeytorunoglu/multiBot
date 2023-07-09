package com.zorbeytorunoglu.multiBot.transcript

import java.awt.Color
import java.util.regex.Pattern

object Formatter {

    private val strong = Pattern.compile("\\*\\*(.+?)\\*\\*")
    private val em = Pattern.compile("\\*(.+?)\\*")
    private val s = Pattern.compile("~~(.+?)~~")
    private val u = Pattern.compile("__(.+?)__")
    private val code = Pattern.compile("```(.+?)```")
    private val code_1 = Pattern.compile("`(.+?)`")
    private val newLine = Pattern.compile("\\n")

    fun formatBytes(bytes: Long): String {
        val unit = 1024
        if (bytes < unit) return "$bytes B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
        val pre = "KMGTPE"[exp - 1].toString() + ""
        return String.format("%.1f %sB", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
    }

    fun format(originalText: String): String {
        var matcher = strong.matcher(originalText)
        var newText = originalText
        while (matcher.find()) {
            val group = matcher.group()
            newText = newText.replace(
                group,
                "<strong>" + group.replace("**", "") + "</strong>"
            )
        }
        matcher = em.matcher(newText)
        while (matcher.find()) {
            val group = matcher.group()
            newText = newText.replace(
                group,
                "<em>" + group.replace("*", "") + "</em>"
            )
        }
        matcher = s.matcher(newText)
        while (matcher.find()) {
            val group = matcher.group()
            newText = newText.replace(
                group,
                "<s>" + group.replace("~~", "") + "</s>"
            )
        }
        matcher = u.matcher(newText)
        while (matcher.find()) {
            val group = matcher.group()
            newText = newText.replace(
                group,
                "<u>" + group.replace("__", "") + "</u>"
            )
        }
        matcher = code.matcher(newText)
        var findCode = false
        while (matcher.find()) {
            val group = matcher.group()
            newText = newText.replace(
                group,
                "<div class=\"pre pre--multiline nohighlight\">"
                        + group.replace("```", "").substring(3, -3) + "</div>"
            )
            findCode = true
        }
        if (!findCode) {
            matcher = code_1.matcher(newText)
            while (matcher.find()) {
                val group = matcher.group()
                newText = newText.replace(
                    group,
                    "<span class=\"pre pre--inline\">" + group.replace("`", "") + "</span>"
                )
            }
        }
        matcher = newLine.matcher(newText)
        while (matcher.find()) newText = newText.replace(matcher.group(), "<br />")
        return newText
    }

    fun toHex(color: Color): String {
        var hex = Integer.toHexString(color.rgb and 0xffffff)
        while (hex.length < 6) hex = "0$hex"
        return hex
    }
}