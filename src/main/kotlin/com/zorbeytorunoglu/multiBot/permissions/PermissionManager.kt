package com.zorbeytorunoglu.multiBot.permissions

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.dv8tion.jda.api.entities.Member
import java.io.File
import java.io.FileWriter
import java.nio.file.Files

class PermissionManager {

    var permissions = HashMap<String, PermissionData>()

    init {
        loadPermissions()
    }

    private fun loadPermissions() {

        val file = File("permissions.json")

        if (!file.exists()) return

        try {
            val reader = Files.newBufferedReader(file.toPath())

            val type = object : TypeToken<HashMap<String, PermissionData>>() {}.type

            permissions = Gson().fromJson(reader, type)
            reader.close()
        } catch (e: Exception) {
            println("Permissions could not be loaded.")
            e.printStackTrace()
        }

    }

    fun savePermissions() {

        if (permissions.isEmpty()) return

        val gson = GsonBuilder().setPrettyPrinting().create()

        try {
            val writer = FileWriter("permissions.json")
            writer.write(gson.toJson(permissions))
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
            println("Permissions could not be saved due to an error in writer.")
        }

    }

    fun hasPermission(id: String, permission: Permission): Boolean {
        if (!permissions.containsKey(id)) return false
        return permissions[id]!!.permissions.contains(permission)
    }

    fun hasPermission(member: Member, permission: Permission): Boolean {
        if (member.permissions.contains(net.dv8tion.jda.api.Permission.ADMINISTRATOR)) return true
        if (!permissions.containsKey(member.id)) return false
        return permissions[member.id]!!.permissions.contains(permission)
    }

    fun addPermission(id: String, holderType: HolderType, permission: Permission) {
        val collection = mutableListOf<Permission>()
        if (permissions.containsKey(id)) collection.addAll(permissions[id]!!.permissions)
        collection.add(permission)
        permissions[id] = PermissionData(holderType, collection)
    }

    fun removePermission(id: String, permission: Permission) {
        if (!permissions.containsKey(id)) return
        val collection = permissions[id]!!.permissions
        if (!collection.contains(permission)) return
        collection.remove(permission)
    }

}