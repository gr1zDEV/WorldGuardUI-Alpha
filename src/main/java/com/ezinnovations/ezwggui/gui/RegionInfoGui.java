package com.ezinnovations.ezwggui.gui;

import com.ezinnovations.ezwggui.EzWGGUI;
import com.ezinnovations.ezwggui.hooks.WorldGuardHook;
import com.ezinnovations.ezwggui.util.GuiUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class RegionInfoGui extends BaseGui {
    private final EzWGGUI plugin;
    private final WorldGuardHook.RegionRef ref;
    private final Inventory inventory;

    public RegionInfoGui(EzWGGUI plugin, WorldGuardHook.RegionRef ref) {
        this.plugin = plugin;
        this.ref = ref;
        this.inventory = Bukkit.createInventory(this, 54, Component.text("Region: " + ref.region().getId()));
        render();
    }

    private void render() {
        GuiUtil.applyBorder(inventory, new GuiUtil.ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(Component.text(" ")).build());
        inventory.setItem(10, new GuiUtil.ItemBuilder(Material.WRITABLE_BOOK).name(Component.text("Edit Flags")).build());
        inventory.setItem(12, new GuiUtil.ItemBuilder(Material.PLAYER_HEAD).name(Component.text("Members")).build());
        inventory.setItem(14, new GuiUtil.ItemBuilder(Material.GOLDEN_HELMET).name(Component.text("Owners")).build());
        inventory.setItem(16, new GuiUtil.ItemBuilder(Material.COMPASS).name(Component.text("Teleport")).build());
        inventory.setItem(22, new GuiUtil.ItemBuilder(Material.OAK_SIGN).name(Component.text(ref.region().getId(), NamedTextColor.GOLD))
                .lore(GuiUtil.lore(
                        Component.text("World: " + ref.world().getName(), NamedTextColor.GRAY),
                        Component.text("Min: " + ref.region().getMinimumPoint(), NamedTextColor.GRAY),
                        Component.text("Max: " + ref.region().getMaximumPoint(), NamedTextColor.GRAY),
                        Component.text("Priority: " + ref.region().getPriority(), NamedTextColor.YELLOW)
                )).build());
        inventory.setItem(31, new GuiUtil.ItemBuilder(Material.REDSTONE).name(Component.text("Priority")).build());
        inventory.setItem(49, new GuiUtil.ItemBuilder(Material.ARROW).name(Component.text("Back")).build());
    }

    @Override
    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        switch (event.getRawSlot()) {
            case 10 -> new FlagEditorGui(plugin, ref, 0).open(player);
            case 12 -> new MemberManagerGui(plugin, ref, false).open(player);
            case 14 -> new MemberManagerGui(plugin, ref, true).open(player);
            case 16 -> player.teleport(plugin.getWorldGuardHook().getRegionCenter(player, ref.region()));
            case 31 -> plugin.getChatInput().request(player, Component.text("Type new priority (or cancel):"), input -> {
                if (input.equalsIgnoreCase("cancel")) {
                    player.sendMessage(Component.text("Cancelled.", NamedTextColor.YELLOW));
                } else {
                    try {
                        plugin.getWorldGuardHook().setPriority(ref, Integer.parseInt(input));
                        player.sendMessage(Component.text("Priority updated.", NamedTextColor.GREEN));
                    } catch (NumberFormatException ex) {
                        player.sendMessage(Component.text("Invalid number.", NamedTextColor.RED));
                    }
                }
                new RegionInfoGui(plugin, ref).open(player);
            });
            case 49 -> new RegionListGui(plugin, 0).open(player);
            default -> {
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
