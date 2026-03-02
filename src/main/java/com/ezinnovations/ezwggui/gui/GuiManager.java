package com.ezinnovations.ezwggui.gui;

import com.ezinnovations.ezwggui.EzWGGUI;
import com.ezinnovations.ezwggui.hooks.WorldGuardHook;
import org.bukkit.entity.Player;

public class GuiManager {
    private final EzWGGUI plugin;

    public GuiManager(EzWGGUI plugin) {
        this.plugin = plugin;
    }

    public void openRegionList(Player player, int page) {
        new RegionListGui(plugin, page).open(player);
    }

    public void openRegionInfo(Player player, WorldGuardHook.RegionRef ref) {
        new RegionInfoGui(plugin, ref).open(player);
    }

    public void openFlags(Player player, WorldGuardHook.RegionRef ref, int page) {
        new FlagEditorGui(plugin, ref, page).open(player);
    }

    public void openMembers(Player player, WorldGuardHook.RegionRef ref, boolean owners) {
        new MemberManagerGui(plugin, ref, owners).open(player);
    }
}
