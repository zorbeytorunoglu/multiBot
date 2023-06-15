package com.zorbeytorunoglu.multiBot.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileWriter
import java.nio.file.Files

object GsonUtils {

    fun <T> loadFromJson(file: File, clazz: Class<T>): T {

        var returnClazz = clazz.newInstance()

        if (file.exists()) {
            try {
                val reader = Files.newBufferedReader(file.toPath().toAbsolutePath())

                val typeToken = TypeToken.getParameterized(clazz).type

                returnClazz = Gson().fromJson(reader.readText(), typeToken)

                reader.close()
            } catch (e: Exception) {
                println("${file.name} could not be loaded.")
                e.printStackTrace()
            }
        } else {

            file.createNewFile()

            val gson = GsonBuilder().setPrettyPrinting().create()
            try {
                val writer = FileWriter(file)
                writer.write(gson.toJson(clazz.newInstance()))
                writer.close()
            } catch (e: Exception) {
                e.printStackTrace()
                println("${file.name} could not be saved due to an error in writer.")
            }

        }

        return returnClazz

    }

}