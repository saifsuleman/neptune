package net.saifs.neptune.core.extensions

import net.kyori.adventure.text.Component
import net.saifs.neptune.util.parseMini
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class StackBuilder internal constructor(material: Material) {
    private var item: ItemStack = ItemStack(material, 1)

    fun amount(amount: Int) {
        item.amount = amount
    }

    fun name(name: Component) {
        meta<ItemMeta> {
            displayName(name)
        }
    }

    fun lore(lore: List<Component>) {
        meta<ItemMeta> {
            lore(lore)
        }
    }

    fun name(name: String) {
        meta<ItemMeta> {
            displayName(parseMini(name))
        }
    }

    inline fun <reified T : ItemMeta> meta(block: T.() -> Unit) {
        val item = build()
        item.itemMeta = (item.itemMeta as? T)?.apply(block)
    }

    fun build(): ItemStack {
        return item
    }
}

fun stackOf(material: Material, block: StackBuilder.() -> Unit): ItemStack {
    val builder = StackBuilder(material)
    builder.apply(block)
    return builder.build()
}