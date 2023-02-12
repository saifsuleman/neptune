package net.saifs.neptune.core.gui;

import net.saifs.neptune.Neptune;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PolyMenu {
    private final Supplier<String> title;
    private final List<MenuPartition> partitions;
    private final List<Consumer<Player>> terminables;
    private final Set<Player> viewers;
    private final Set<Player> refreshing;
    private Map<Integer, PolyMenuItem> items;
    private int partitioned;

    public PolyMenu(Supplier<String> title) {
        this.title = title;

        this.items = new HashMap<>();
        this.partitioned = 0;
        this.terminables = new ArrayList<>();
        this.partitions = new ArrayList<>();
        this.viewers = new HashSet<>();
        this.refreshing = new HashSet<>();
    }

    void setSlot(int slot, PolyMenuItem menuItem) {
        if (menuItem == null) {
            this.items.remove(slot);
            return;
        }
        this.items.put(slot, menuItem);
    }

    public int getPartitioned() {
        return partitioned;
    }

    public void setPartitioned(int partitioned) {
        this.partitioned = partitioned;
    }

    public void destroy() {
        Neptune.Companion.getInstance().getMenuSystem().removeMenu(this);
    }

    public void onClose(Consumer<Player> terminable) {
        this.terminables.add(terminable);
    }

    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);

        if (e.getClickedInventory() == null || e.getClickedInventory().getType() != InventoryType.CHEST) {
            return;
        }

        int slot = e.getSlot();
        if (this.items.containsKey(slot)) {
            PolyMenuItem item = this.items.get(slot);
            item.handleClick((Player) e.getWhoClicked(), e.getClick());
        }
    }

    public void close(Player player) {
        if (this.refreshing.remove(player) || !this.viewers.remove(player)) {
            return;
        }

        this.terminables.forEach(terminable -> terminable.accept(player));
    }

    public void refresh() {
        viewers.forEach(this::refresh);
    }

    public void newPartition(String format, Consumer<MenuPartition> consumer) {
        MenuPartition partition = new MenuPartition(this, format, consumer);
        this.partitions.add(partition);
    }

    private Inventory build() {
        this.items = new HashMap<>();
        this.partitioned = 0;

        for (MenuPartition partition : this.partitions) {
            partition.refresh();
        }

        int rows = Math.min((int) Math.ceil((Collections.max(this.items.keySet()) + 1) / 9.0), 6);
        int size = rows * 9;

        Inventory inventory = Bukkit.createInventory(null, size, this.title.get());
        for (Map.Entry<Integer, PolyMenuItem> entry : this.items.entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue().getItemStack());
        }

        return inventory;
    }

    public void open(Player player) {
        Inventory inventory = this.build();
        Neptune.Companion.getInstance().getMenuSystem().close(player);
        player.openInventory(inventory);
        this.viewers.add(player);
    }

    public void refresh(Player player) {
        this.refreshing.add(player);
        player.openInventory(this.build());
    }

    public Set<Player> getViewers() {
        return viewers;
    }

    public boolean isViewing(Player player) {
        return this.viewers.contains(player);
    }
}
