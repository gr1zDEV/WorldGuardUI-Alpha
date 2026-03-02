package com.ezinnovations.ezwggui.gui;

import com.ezinnovations.ezwggui.EzWGGUI;
import com.ezinnovations.ezwggui.hooks.WorldGuardHook;
import com.ezinnovations.ezwggui.util.GuiUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class RegionListGui extends BaseGui {
    private final EzWGGUI plugin;
    private final int page;
    private final Inventory inventory;
    private final List<WorldGuardHook.RegionSummary> regions;

    public RegionListGui(EzWGGUI plugin, int page) {
        this.plugin = plugin;
        this.page = Math.max(0, page);
        this.inventory = Bukkit.createInventory(this, 54, Component.text("EzWGGUI — Regions"));
        this.regions = plugin.getWorldGuardHook().getAllRegions();
        render();
    }

    private void render() {
        GuiUtil.applyBorder(inventory, new GuiUtil.ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(Component.text(" ")).build());
        int perPage = plugin.getConfig().getInt("gui.items-per-page", 28);
        int start = page * perPage;
        int[] slots = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43};
        for (int i = 0; i < slots.length && start + i < regions.size(); i++) {
            WorldGuardHook.RegionSummary region = regions.get(start + i);
            inventory.setItem(slots[i], new GuiUtil.ItemBuilder(Material.OAK_SIGN)
                    .name(Component.text(region.id(), NamedTextColor.GOLD))
                    .lore(GuiUtil.lore(
                            Component.text("World: " + region.world(), NamedTextColor.GRAY),
                            Component.text("Priority: " + region.priority(), NamedTextColor.YELLOW),
                            Component.text("Members: " + region.members() + " Owners: " + region.owners(), NamedTextColor.AQUA)
                    )).build());
        }
        if (page > 0) inventory.setItem(48, new GuiUtil.ItemBuilder(Material.ARROW).name(Component.text("Previous")).build());
        if (start + perPage < regions.size()) inventory.setItem(50, new GuiUtil.ItemBuilder(Material.ARROW).name(Component.text("Next")).build());
        inventory.setItem(49, new GuiUtil.ItemBuilder(Material.BARRIER).name(Component.text("Close", NamedTextColor.RED)).build());
    }

    @Override
    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        int slot = event.getRawSlot();
        if (slot == 49) {
            player.closeInventory();
            return;
        }
        if (slot == 48 && page > 0) {
            new RegionListGui(plugin, page - 1).open(player);
            return;
        }
        if (slot == 50) {
            new RegionListGui(plugin, page + 1).open(player);
            return;
        }

        if (slot < 54 && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.OAK_SIGN && event.getCurrentItem().getItemMeta() != null) {
            String regionName = PlainTextComponentSerializer.plainText().serialize(event.getCurrentItem().getItemMeta().displayName());
            plugin.getWorldGuardHook().findRegion(regionName).ifPresent(ref -> new RegionInfoGui(plugin, ref).open(player));
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
