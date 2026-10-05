package cz.setspawnplus;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public final class SpawnListener implements Listener {

    private final SetSpawnPlugin plugin;

    public SpawnListener(SetSpawnPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRespawn(PlayerRespawnEvent event) {
        FileConfiguration cfg = plugin.getConfig();
        if (!cfg.getBoolean("respawn.enabled", true)) return;

        boolean keepBed = event.isBedSpawn() && !cfg.getBoolean("respawn.ignore-bed", false);
        boolean keepAnchor = event.isAnchorSpawn() && !cfg.getBoolean("respawn.ignore-anchor", false);
        if (keepBed || keepAnchor) return;

        Location spawn = plugin.spawns().getSpawn(event.getPlayer().getWorld());
        if (spawn != null) event.setRespawnLocation(spawn);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FileConfiguration cfg = plugin.getConfig();
        boolean first = !player.hasPlayedBefore();

        if (first && cfg.getBoolean("first-join.enabled", true)) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;
                Location target = plugin.spawns().getFirstSpawn();
                if (target == null && cfg.getBoolean("first-join.fallback-to-spawn", true)) {
                    target = plugin.spawns().getSpawn(player.getWorld());
                }
                if (target != null) player.teleport(target);

                for (String command : cfg.getStringList("first-join.commands")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
                }
                String broadcast = cfg.getString("first-join.broadcast", "");
                if (broadcast != null && !broadcast.isEmpty()) {
                    Bukkit.broadcast(Messages.color(broadcast.replace("%player%", player.getName())));
                }
            });
        } else if (!first && cfg.getBoolean("spawn.teleport-on-join", false)) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                Location spawn = plugin.spawns().getSpawn(player.getWorld());
                if (player.isOnline() && spawn != null) player.teleport(spawn);
            });
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!plugin.getConfig().getBoolean("teleport.cancel-on-move", true)) return;
        Player player = event.getPlayer();
        if (!plugin.teleports().isPending(player)) return;

        Location origin = plugin.teleports().originOf(player);
        Location to = event.getTo();
        if (origin == null || to == null) return;
        if (origin.getBlockX() != to.getBlockX()
                || origin.getBlockY() != to.getBlockY()
                || origin.getBlockZ() != to.getBlockZ()) {
            plugin.teleports().cancel(player, true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.getConfig().getBoolean("teleport.cancel-on-damage", true)) return;
        if (plugin.teleports().isPending(player)) plugin.teleports().cancel(player, true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.teleports().forget(event.getPlayer());
    }
}
