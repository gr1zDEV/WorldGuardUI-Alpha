package com.ezinnovations.ezwggui.util;

import com.ezinnovations.ezwggui.EzWGGUI;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ChatInput implements Listener {
    private final EzWGGUI plugin;
    private final Map<UUID, Consumer<String>> pendingInput = new ConcurrentHashMap<>();

    public ChatInput(EzWGGUI plugin) {
        this.plugin = plugin;
    }

    public void request(Player player, Component prompt, Consumer<String> callback) {
        pendingInput.put(player.getUniqueId(), callback);
        player.closeInventory();
        player.sendMessage(prompt);
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        Consumer<String> callback = pendingInput.remove(player.getUniqueId());
        if (callback == null) {
            return;
        }

        event.setCancelled(true);
        String text = PlainTextComponentSerializer.plainText().serialize(event.message());
        plugin.getSchedulerUtil().runTask(player, () -> callback.accept(text));
    }
}
