package com.ezinnovations.ezwggui;

import com.ezinnovations.ezwggui.commands.WGGUICommand;
import com.ezinnovations.ezwggui.hooks.WorldGuardHook;
import com.ezinnovations.ezwggui.listeners.GuiClickListener;
import com.ezinnovations.ezwggui.util.ChatInput;
import com.ezinnovations.ezwggui.util.SchedulerUtil;
import org.bukkit.plugin.java.JavaPlugin;

public class EzWGGUI extends JavaPlugin {
    private WorldGuardHook worldGuardHook;
    private SchedulerUtil schedulerUtil;
    private ChatInput chatInput;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.schedulerUtil = new SchedulerUtil(this);
        this.worldGuardHook = new WorldGuardHook(this);
        this.chatInput = new ChatInput(this);

        getServer().getPluginManager().registerEvents(new GuiClickListener(this), this);
        getServer().getPluginManager().registerEvents(chatInput, this);

        WGGUICommand command = new WGGUICommand(this);
        if (getCommand("ezwggui") != null) {
            getCommand("ezwggui").setExecutor(command);
            getCommand("ezwggui").setTabCompleter(command);
        }

        if (!worldGuardHook.isAvailable()) {
            getLogger().warning("WorldGuard not found. GUI functionality will be unavailable.");
        }
    }

    public WorldGuardHook getWorldGuardHook() {
        return worldGuardHook;
    }

    public SchedulerUtil getSchedulerUtil() {
        return schedulerUtil;
    }

    public ChatInput getChatInput() {
        return chatInput;
    }
}
