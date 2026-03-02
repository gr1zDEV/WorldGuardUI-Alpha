package com.ezinnovations.ezwggui.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class WorldGuardHook {
    private final Plugin plugin;
    private final boolean available;

    public WorldGuardHook(Plugin plugin) {
        this.plugin = plugin;
        this.available = Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
    }

    public boolean isAvailable() {
        return available;
    }

    public List<RegionSummary> getAllRegions() {
        List<RegionSummary> regions = new ArrayList<>();
        if (!available) return regions;

        for (World world : Bukkit.getWorlds()) {
            RegionManager manager = getRegionManager(world);
            if (manager == null) continue;
            for (ProtectedRegion region : manager.getRegions().values()) {
                regions.add(new RegionSummary(region.getId(), world.getName(), region.getPriority(), region.getMembers().size(), region.getOwners().size()));
            }
        }
        regions.sort(Comparator.comparing(RegionSummary::id));
        return regions;
    }

    public Optional<RegionRef> findRegion(String regionName) {
        if (!available) return Optional.empty();
        for (World world : Bukkit.getWorlds()) {
            RegionManager manager = getRegionManager(world);
            if (manager == null) continue;
            ProtectedRegion region = manager.getRegion(regionName);
            if (region != null) return Optional.of(new RegionRef(world, manager, region));
        }
        return Optional.empty();
    }

    public Collection<Flag<?>> getAllFlagsSorted() {
        List<Flag<?>> flags = new ArrayList<>(WorldGuard.getInstance().getFlagRegistry().getAll());
        List<String> common = List.of("pvp", "build", "block-break", "block-place", "interact", "chest-access", "use", "entry", "exit");
        flags.sort(Comparator.comparingInt(f -> {
            int idx = common.indexOf(f.getName());
            return idx >= 0 ? idx : common.size();
        }).thenComparing(Flag::getName));
        return flags;
    }

    public Object getFlagValue(ProtectedRegion region, Flag<?> flag) {
        return region.getFlag(flag);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean cycleStateFlag(RegionRef ref, Flag<?> flag) {
        if (!(flag instanceof StateFlag stateFlag)) {
            return false;
        }
        StateFlag.State current = ref.region().getFlag(stateFlag);
        StateFlag.State next = current == null ? StateFlag.State.ALLOW : current == StateFlag.State.ALLOW ? StateFlag.State.DENY : null;
        ref.region().setFlag((Flag) stateFlag, next);
        save(ref);
        return true;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean setFlagFromInput(RegionRef ref, Flag<?> flag, String input) {
        try {
            Object value;
            if (flag instanceof StateFlag) {
                value = "allow".equalsIgnoreCase(input) ? StateFlag.State.ALLOW : "deny".equalsIgnoreCase(input) ? StateFlag.State.DENY : null;
            } else if (flag instanceof IntegerFlag) {
                value = Integer.parseInt(input);
            } else if (flag instanceof StringFlag) {
                value = input;
            } else {
                FlagContext context = FlagContext.create().setInput(input).build();
                value = flag.parseInput(context);
            }
            ref.region().setFlag((Flag) flag, value);
            save(ref);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean unsetFlag(RegionRef ref, Flag<?> flag) {
        ref.region().setFlag(flag, null);
        save(ref);
        return true;
    }

    public void setPriority(RegionRef ref, int priority) {
        ref.region().setPriority(priority);
        save(ref);
    }

    public Location getRegionCenter(Player player, ProtectedRegion region) {
        double x = (region.getMinimumPoint().x() + region.getMaximumPoint().x()) / 2.0;
        double z = (region.getMinimumPoint().z() + region.getMaximumPoint().z()) / 2.0;
        double y = player.getWorld().getHighestBlockYAt((int) x, (int) z) + 1;
        return new Location(player.getWorld(), x, y, z);
    }

    public Map<String, String> defaultFlagDescriptions() {
        Map<String, String> map = new HashMap<>();
        map.put("block-break", "Whether blocks can be mined");
        map.put("block-place", "Whether blocks can be placed");
        map.put("build", "Overall build access control");
        map.put("interact", "Whether players can interact with blocks");
        map.put("chest-access", "Whether containers may be opened");
        map.put("pvp", "Whether PvP combat is allowed");
        map.put("entry", "Whether players can enter region");
        map.put("exit", "Whether players can leave region");
        map.put("use", "Whether use actions are allowed");
        for (Flag<?> flag : getAllFlagsSorted()) {
            map.putIfAbsent(flag.getName().toLowerCase(Locale.ROOT), "WorldGuard flag: " + flag.getName());
        }
        return map;
    }

    private RegionManager getRegionManager(World world) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
    }

    private void save(RegionRef ref) {
        try {
            ref.regionManager().saveChanges();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save WorldGuard changes: " + e.getMessage());
        }
    }

    public record RegionSummary(String id, String world, int priority, int members, int owners) {}

    public record RegionRef(World world, RegionManager regionManager, ProtectedRegion region) {
        public RegionRef {
            Objects.requireNonNull(world);
            Objects.requireNonNull(regionManager);
            Objects.requireNonNull(region);
        }
    }
}
