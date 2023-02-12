package net.saifs.neptune.core.gui;

import net.saifs.neptune.Neptune;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

// newMenu
// click handler
// close handler
public class MenuSystem implements Listener {
    private final List<PolyMenu> menus;
    private final Neptune plugin;

    public MenuSystem() {
        this.menus = new ArrayList<>();
        plugin = Neptune.Companion.getInstance();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public PolyMenu createMenu(Supplier<String> title) {
        PolyMenu menu = new PolyMenu(title);
        this.menus.add(menu);
        return menu;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        for (PolyMenu menu : this.menus) {
            if (menu.isViewing(player)) {
                menu.onClick(e);
                break;
            }
        }
    }

    public void removeMenu(PolyMenu menu) {
        this.menus.remove(menu);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        close(player);
    }

    public void close(Player player) {
        for (PolyMenu menu : this.menus) {
            if (menu.isViewing(player)) {
                Bukkit.getScheduler().runTask(plugin, () -> menu.close(player));
                break;
            }
        }
    }
}