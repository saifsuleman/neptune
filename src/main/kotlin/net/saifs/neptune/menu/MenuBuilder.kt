package net.saifs.neptune.menu

import net.kyori.adventure.text.Component
import net.saifs.neptune.extensions.stackOf
import net.saifs.neptune.util.parseMini
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class MenuBuilder {
    val items: MutableMap<Int, MenuItem> = mutableMapOf()
    val terminables: MutableList<(Player) -> Unit> = mutableListOf()
    private var title: Component? = null
    private var size: Int = 54

    fun title(title: Component?) {
        this.title = title
    }

    fun title(title: String) {
        this.title = parseMini(title)
    }

    fun title(): Component? {
        return this.title
    }

    fun size(size: Int) {
        this.size = size
    }

    fun size(): Int {
        return this.size
    }

    fun fill() {
        fill(stackOf(Material.AIR))
    }

    fun fill(item: ItemStack) {
        for (i in 0 until size) {
            items[i] = MenuItem(item.clone())
        }
    }

    fun slot(index: Int, item: ItemStack) {
        items[index] = MenuItem(item.clone())
    }

    fun slot(x: Int, y: Int, item: ItemStack) {
        slot(y * 9 + x, item)
    }

    fun slot(index: Int, item: ItemStack, handler: MenuClickHandler) {
        items[index] = MenuItem(item.clone(), handler)
    }

    fun slot(x: Int, y: Int, item: ItemStack, handler: MenuClickHandler) {
        slot(y * 9 + x, item, handler)
    }

    fun partition(mask: String, block: PartitionBuilder.() -> Unit) {
        val builder = PartitionBuilder(this, mask)
        builder.apply(block)
    }

    fun onClose(handler: (Player) -> Unit) {
        terminables.add(handler)
    }
}

class PartitionBuilder(val builder: MenuBuilder, val mask: String) {
    private val slots: Queue<Int> = ConcurrentLinkedQueue()

    init {
        var slot = 0
        for (char in mask) when (char) {
            '0' -> {
                slot++
            }

            ' ' -> {
                if (slot % 9 == 0) {
                    continue
                }
                slot += 9 - (slot % 9)
            }

            '\n' -> {
                slot += 9 - (slot % 9)
            }

            '1' -> {
                slots.add(slot)
                slot++
            }

            '~' -> {
                while ((slot % 9) > 0) {
                    slots.add(slot)
                    slot++
                }
            }
        }
    }

    fun slot(item: ItemStack) {
        val slot = slots.poll() ?: return
        builder.slot(slot, item)
    }

    fun slot(item: ItemStack, handler: MenuClickHandler) {
        val slot = slots.poll() ?: return
        builder.slot(slot, item, handler)
    }
}