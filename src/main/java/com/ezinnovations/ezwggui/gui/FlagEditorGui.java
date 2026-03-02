package com.ezinnovations.ezwggui.gui;

import com.ezinnovations.ezwggui.EzWGGUI;
import com.ezinnovations.ezwggui.hooks.WorldGuardHook;
import com.ezinnovations.ezwggui.util.GuiUtil;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FlagEditorGui extends BaseGui {
    private final EzWGGUI plugin;
    private final WorldGuardHook.RegionRef ref;
    private final int page;
    private final Inventory inventory;
    private final List<Flag<?>> flags;
    private final Map<String, String> descriptions;

    public FlagEditorGui(EzWGGUI plugin, WorldGuardHook.RegionRef ref, int page) {
        this.plugin = plugin;
        this.ref = ref;
        this.page = Math.max(0, page);
        this.inventory = Bukkit.createInventory(this, 54, Component.text("Edit flags"));
        this.flags = new ArrayList<>(plugin.getWorldGuardHook().getAllFlagsSorted());
        this.descriptions = plugin.getWorldGuardHook().defaultFlagDescriptions();
        render();
    }

    private void render() {
        GuiUtil.applyBorder(inventory, new GuiUtil.ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(Component.text(" ")).build());
        int[] slots = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43};
        int perPage = plugin.getConfig().getInt("gui.items-per-page", 28);
        int start = page * perPage;

        for (int i = 0; i < slots.length && start + i < flags.size(); i++) {
            Flag<?> flag = flags.get(start + i);
            Object value = plugin.getWorldGuardHook().getFlagValue(ref.region(), flag);
            NamedTextColor valueColor = value == null ? NamedTextColor.YELLOW :
                    value == StateFlag.State.ALLOW ? NamedTextColor.GREEN :
                    value == StateFlag.State.DENY ? NamedTextColor.RED : NamedTextColor.YELLOW;
            inventory.setItem(slots[i], new GuiUtil.ItemBuilder(Material.REDSTONE_TORCH)
                    .name(Component.text(flag.getName() + " (#0076)", NamedTextColor.AQUA, TextDecoration.BOLD))
                    .lore(GuiUtil.lore(
                            Component.text("Value:", NamedTextColor.GOLD),
                            Component.text(String.valueOf(value == null ? "UNSET" : value), valueColor),
                            Component.text(descriptions.getOrDefault(flag.getName(), "WorldGuard flag"), NamedTextColor.GRAY, TextDecoration.ITALIC),
                            Component.text("minecraft:redstone_torch", NamedTextColor.DARK_GRAY),
                            Component.text("NBT: none", NamedTextColor.DARK_GRAY)
                    )).build());
        }
        if (page > 0) inventory.setItem(45, new GuiUtil.ItemBuilder(Material.PAPER).name(Component.text("Previous Page")).build());
        if (start + perPage < flags.size()) inventory.setItem(53, new GuiUtil.ItemBuilder(Material.PAPER).name(Component.text("Next Page")).build());
        inventory.setItem(49, new GuiUtil.ItemBuilder(Material.RED_CONCRETE).name(Component.text("Back", NamedTextColor.RED)).build());
        inventory.setItem(50, new GuiUtil.ItemBuilder(Material.CRAFTING_TABLE).name(Component.text("Quick Presets")).build());
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
        int[] slots = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43};

        if (slot == 49) { new RegionInfoGui(plugin, ref).open(player); return; }
        if (slot == 45 && page > 0) { new FlagEditorGui(plugin, ref, page - 1).open(player); return; }
        if (slot == 53) { new FlagEditorGui(plugin, ref, page + 1).open(player); return; }
        if (slot == 50) { applyPreset("safe-zone"); player.sendMessage(Component.text("Applied preset: safe-zone", NamedTextColor.GREEN)); new FlagEditorGui(plugin, ref, page).open(player); return; }

        int idx = -1;
        for (int i = 0; i < slots.length; i++) if (slots[i] == slot) idx = i;
        if (idx == -1) return;
        int actual = page * plugin.getConfig().getInt("gui.items-per-page", 28) + idx;
        if (actual >= flags.size()) return;

        Flag<?> flag = flags.get(actual);
        if (event.getClick().isShiftClick()) {
            plugin.getWorldGuardHook().unsetFlag(ref, flag);
        } else if (event.getClick() == ClickType.RIGHT) {
            plugin.getChatInput().request(player, Component.text("Type value for " + flag.getName() + " (or cancel):"), input -> {
                if (!input.equalsIgnoreCase("cancel")) {
                    if (!plugin.getWorldGuardHook().setFlagFromInput(ref, flag, input)) {
                        player.sendMessage(Component.text("Invalid value.", NamedTextColor.RED));
                    }
                }
                new FlagEditorGui(plugin, ref, page).open(player);
            });
            return;
        } else {
            plugin.getWorldGuardHook().cycleStateFlag(ref, flag);
        }
        new FlagEditorGui(plugin, ref, page).open(player);
    }

    private void applyPreset(String preset) {
        var section = plugin.getConfig().getConfigurationSection("presets." + preset);
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            plugin.getWorldGuardHook().getAllFlagsSorted().stream()
                    .filter(flag -> flag.getName().equalsIgnoreCase(key))
                    .findFirst()
                    .ifPresent(flag -> plugin.getWorldGuardHook().setFlagFromInput(ref, flag, section.getString(key, "")));
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
