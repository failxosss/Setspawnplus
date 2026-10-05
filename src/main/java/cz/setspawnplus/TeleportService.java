package cz.setspawnplus;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Teleporty s čekáním, cooldownem a rušením při pohybu / poškození. */
public final class TeleportService {

    private final SetSpawnPlugin plugin;
    private final Map<UUID, BukkitTask> pending = new HashMap<>();
    private final Map<UUID, Location> origins = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public TeleportService(SetSpawnPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * @param instant true = ignoruje delay i cooldown (např. admin teleport jiného hráče)
     */
    public void teleport(Player player, Location target, boolean instant) {
        FileConfiguration cfg = plugin.getConfig();
        UUID id = player.getUniqueId();
        int delay = cfg.getInt("teleport.delay", 3);
        int cooldown = cfg.getInt("teleport.cooldown", 10);

        if (!instant && cooldown > 0 && !player.hasPermission("setspawn.bypass.cooldown")) {
            Long last = cooldowns.get(id);
            if (last != null) {
                long left = cooldown - (System.currentTimeMillis() - last) / 1000;
                if (left > 0) {
                    plugin.messages().send(player, "cooldown", "%seconds%", String.valueOf(left));
                    return;
                }
            }
        }

        if (instant || delay <= 0 || player.hasPermission("setspawn.bypass.delay")) {
            finish(player, target, instant);
            return;
        }

        cancel(player, false);
        plugin.messages().send(player, "teleporting", "%seconds%", String.valueOf(delay));
        origins.put(id, player.getLocation());
        pending.put(id, Bukkit.getScheduler().runTaskLater(plugin, () -> {
            pending.remove(id);
            origins.remove(id);
            if (player.isOnline()) finish(player, target, false);
        }, delay * 20L));
    }

    private void finish(Player player, Location target, boolean instant) {
        player.teleport(target);
        if (!instant) cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        plugin.messages().send(player, "teleported");
        String sound = plugin.getConfig().getString("teleport.sound", "");
        if (sound != null && !sound.isEmpty()) {
            player.playSound(player.getLocation(), sound, 1f, 1f);
        }
    }

    public boolean isPending(Player player) {
        return pending.containsKey(player.getUniqueId());
    }

    public Location originOf(Player player) {
        return origins.get(player.getUniqueId());
    }

    public void cancel(Player player, boolean notify) {
        BukkitTask task = pending.remove(player.getUniqueId());
        origins.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
            if (notify) plugin.messages().send(player, "teleport-cancelled");
        }
    }

    public void cancelAll() {
        pending.values().forEach(BukkitTask::cancel);
        pending.clear();
        origins.clear();
    }

    public void forget(Player player) {
        cancel(player, false);
        cooldowns.remove(player.getUniqueId());
    }
}
