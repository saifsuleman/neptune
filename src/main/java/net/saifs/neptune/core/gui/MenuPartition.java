package net.saifs.neptune.core.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class MenuPartition {
    private final PolyMenu menu;
    private final Consumer<MenuPartition> consumer;
    private final String layout;

    private boolean alive;
    private Queue<Integer> slots;

    public MenuPartition(PolyMenu menu, String layout, Consumer<MenuPartition> consumer) {
        this.menu = menu;
        this.layout = layout;
        this.consumer = consumer;

        refresh();
    }

    public void refresh() {
        this.alive = true;
        this.slots = new ConcurrentLinkedQueue<>();
        int slot = 0;

        for (char c : layout.toCharArray()) {
            switch (c) {
                case '0':
                    slot++;
                    break;
                case ' ':
                    if (slot % 9 == 0) {
                        break;
                    }
                case '\n':
                    slot += 9 - (slot % 9);
                    break;
                case '1':
                    this.slots.add(slot);
                    slot++;
                    break;
                case '~':
                    while ((slot % 9) > 0) {
                        this.slots.add(slot);
                        slot++;
                    }
                    break;
                case '*':
                    throw new IllegalArgumentException("menu char '*' not implemented yet.");
                default:
                    break;
            }
        }

        this.consumer.accept(this);
        if (alive) menu.setPartitioned(Math.max(slot, menu.getPartitioned()));
    }

    public int newSlot(ItemStack itemStack) {
        if (!alive) return -1;
        int slot = slot();
        PolyMenuItem item = new PolyMenuItem(itemStack);
        this.menu.setSlot(slot, item);
        return slot;
    }

    public int newSlot(ItemStack itemStack, PolyMenuItem.MenuClickFunction handler) {
        if (!alive) return -1;
        int slot = slot();
        PolyMenuItem item = new PolyMenuItem(itemStack, handler);
        this.menu.setSlot(slot, item);
        return slot;
    }

    private int slot() {
        Integer integer = slots.poll();
        if (integer == null) return -1;
        return integer;
    }

    public Queue<Integer> getSlots() {
        return slots;
    }
}
