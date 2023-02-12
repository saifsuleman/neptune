package net.saifs.neptune.core.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class PolyMenuItem {
    private final ItemStack itemStack;
    private MenuClickFunction clickHandler;

    public PolyMenuItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public PolyMenuItem(ItemStack itemStack, MenuClickFunction clickHandler) {
        this.itemStack = itemStack;
        this.clickHandler = clickHandler;
    }

    public void handleClick(Player player, ClickType clickType) {
        if (this.clickHandler != null) {
            this.clickHandler.handleClick(player, clickType);
        }
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    @FunctionalInterface
    public interface MenuClickFunction {
        void handleClick(Player player, ClickType clickType);
    }
}
