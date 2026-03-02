package com.ezinnovations.ezwggui.util;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class GuiUtil {
    private GuiUtil() {
    }

    public static void applyBorder(Inventory inventory, ItemStack pane) {
        for (int i = 0; i < inventory.getSize(); i++) {
            int row = i / 9;
            int col = i % 9;
            if (row == 0 || row == 5 || col == 0 || col == 8) {
                inventory.setItem(i, pane);
            }
        }
    }

    public static class ItemBuilder {
        private final ItemStack itemStack;
        private final ItemMeta meta;

        public ItemBuilder(Material material) {
            this.itemStack = new ItemStack(material);
            this.meta = itemStack.getItemMeta();
        }

        public ItemBuilder name(Component name) {
            meta.displayName(name);
            return this;
        }

        public ItemBuilder lore(List<Component> lore) {
            meta.lore(lore);
            return this;
        }

        public ItemBuilder hideAttributes() {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            return this;
        }

        public ItemStack build() {
            itemStack.setItemMeta(meta);
            return itemStack;
        }
    }

    public static List<Component> lore(Component... lines) {
        List<Component> lore = new ArrayList<>();
        for (Component line : lines) {
            lore.add(line);
        }
        return lore;
    }
}
