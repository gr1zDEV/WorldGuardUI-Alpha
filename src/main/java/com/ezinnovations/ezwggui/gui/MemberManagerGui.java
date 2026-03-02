package com.ezinnovations.ezwggui.gui;

import com.ezinnovations.ezwggui.EzWGGUI;
import com.ezinnovations.ezwggui.hooks.WorldGuardHook;
import com.ezinnovations.ezwggui.util.GuiUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MemberManagerGui extends BaseGui {
    private final EzWGGUI plugin;
    private final WorldGuardHook.RegionRef ref;
    private final boolean ownersMode;
    private final Inventory inventory;
    private final List<String> names;

    public MemberManagerGui(EzWGGUI plugin, WorldGuardHook.RegionRef ref, boolean ownersMode) {
        this.plugin = plugin;
        this.ref = ref;
        this.ownersMode = ownersMode;
        this.inventory = Bukkit.createInventory(this, 54, Component.text((ownersMode ? "Owners: " : "Members: ") + ref.region().getId()));
        Set<String> players = ownersMode ? ref.region().getOwners().getPlayers() : ref.region().getMembers().getPlayers();
        this.names = new ArrayList<>(players);
        render();
    }

    private void render() {
        GuiUtil.applyBorder(inventory, new GuiUtil.ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(Component.text(" ")).build());
        int[] slots = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43};
        for (int i = 0; i < slots.length && i < names.size(); i++) {
            String name = names.get(i);
            inventory.setItem(slots[i], new GuiUtil.ItemBuilder(Material.PLAYER_HEAD)
                    .name(Component.text(name, NamedTextColor.AQUA))
                    .lore(GuiUtil.lore(Component.text("Click to remove", NamedTextColor.GRAY))).build());
        }
        inventory.setItem(45, new GuiUtil.ItemBuilder(Material.ARROW).name(Component.text("Back")).build());
        inventory.setItem(49, new GuiUtil.ItemBuilder(Material.EMERALD).name(Component.text("Add " + (ownersMode ? "owner" : "member"))).build());
    }

    @Override
    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (event.getRawSlot() == 45) { new RegionInfoGui(plugin, ref).open(player); return; }
        if (event.getRawSlot() == 49) {
            plugin.getChatInput().request(player, Component.text("Type player name to add (or cancel):"), input -> {
                if (!input.equalsIgnoreCase("cancel")) {
                    OfflinePlayer target = Bukkit.getOfflinePlayer(input);
                    if (ownersMode) ref.region().getOwners().addPlayer(target.getUniqueId());
                    else ref.region().getMembers().addPlayer(target.getUniqueId());
                    try { ref.regionManager().saveChanges(); } catch (Exception ignored) {}
                }
                new MemberManagerGui(plugin, ref, ownersMode).open(player);
            });
            return;
        }

        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) return;
        String name = PlainTextComponentSerializer.plainText().serialize(event.getCurrentItem().getItemMeta().displayName());
        OfflinePlayer target = Bukkit.getOfflinePlayer(name);
        if (ownersMode) ref.region().getOwners().removePlayer(target.getUniqueId());
        else ref.region().getMembers().removePlayer(target.getUniqueId());
        try { ref.regionManager().saveChanges(); } catch (Exception ignored) {}
        new MemberManagerGui(plugin, ref, ownersMode).open(player);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
