package net.saifs.neptune.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit

fun broadcast(component: Component, permission: String) {
    for (player in Bukkit.getOnlinePlayers()) {
        if (player.hasPermission(permission)) {
            player.sendMessage(component)
        }
    }
    Bukkit.getServer().consoleSender.sendMessage(component)
}

fun broadcast(message: String, permission: String) = broadcast(parseMini(message), permission)

fun parseMini(string: String): Component {
    return MiniMessage.miniMessage().deserialize(string)
}

fun parseMini(string: String, placeholders: Map<String, Component>): Component {
    val resolver = TagResolver.builder()

    for (key in placeholders.keys) {
        val value = placeholders[key]!!
        resolver.tag(key, Tag.inserting(value))
    }

    return MiniMessage.miniMessage().deserialize(string, resolver.build())
}