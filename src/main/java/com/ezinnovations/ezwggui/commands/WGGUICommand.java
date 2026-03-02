package com.ezinnovations.ezwggui.commands;

import com.ezinnovations.ezwggui.EzWGGUI;
import com.ezinnovations.ezwggui.gui.RegionInfoGui;
import com.ezinnovations.ezwggui.gui.RegionListGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WGGUICommand implements CommandExecutor, TabCompleter {
    private final EzWGGUI plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public WGGUICommand(EzWGGUI plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (!plugin.getWorldGuardHook().isAvailable()) {
            player.sendMessage(msg("messages.worldguard-not-found"));
            return true;
        }
        if (args.length == 0) {
            if (!player.hasPermission("ezwggui.use")) {
                player.sendMessage(msg("messages.no-permission"));
                return true;
            }
            new RegionListGui(plugin, 0).open(player);
            return true;
        }
        if (args[0].equalsIgnoreCase("reload") && player.hasPermission("ezwggui.reload")) {
            plugin.reloadConfig();
            player.sendMessage(Component.text("EzWGGUI reloaded."));
            return true;
        }
        if (args[0].equalsIgnoreCase("region") && args.length > 1) {
            plugin.getWorldGuardHook().findRegion(args[1]).ifPresentOrElse(
                    ref -> new RegionInfoGui(plugin, ref).open(player),
                    () -> player.sendMessage(msg("messages.region-not-found"))
            );
            return true;
        }
        return true;
    }

    private Component msg(String path) {
        String prefix = plugin.getConfig().getString("messages.prefix", "");
        return mm.deserialize(prefix + plugin.getConfig().getString(path, ""));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("reload", "region");
        if (args.length == 2 && args[0].equalsIgnoreCase("region")) {
            return new ArrayList<>(plugin.getWorldGuardHook().getAllRegions().stream().map(r -> r.id()).toList());
        }
        return List.of();
    }
}
