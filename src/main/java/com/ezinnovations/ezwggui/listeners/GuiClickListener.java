package com.ezinnovations.ezwggui.listeners;

import com.ezinnovations.ezwggui.EzWGGUI;
import com.ezinnovations.ezwggui.gui.BaseGui;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GuiClickListener implements Listener {
    @SuppressWarnings("unused")
    private final EzWGGUI plugin;

    public GuiClickListener(EzWGGUI plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof BaseGui gui) {
            gui.handleClick(event);
        }
    }
}
