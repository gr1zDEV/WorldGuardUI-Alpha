package com.ezinnovations.ezwggui.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public abstract class BaseGui implements InventoryHolder {
    public abstract void open(Player player);
    public abstract void handleClick(InventoryClickEvent event);
    @Override
    public abstract Inventory getInventory();
}
