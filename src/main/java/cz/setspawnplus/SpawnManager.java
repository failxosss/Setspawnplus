package cz.setspawnplus;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/** Ukládá a načítá spawny ze spawns.yml. */
public final class SpawnManager {

    private final SetSpawnPlugin plugin;
    private final File file;
    private YamlConfiguration data;

    public SpawnManager(SetSpawnPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "spawns.yml");
        load();
    }

    public void load() {
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    private void save() {
        try {
            plugin.getDataFolder().mkdirs();
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Nepodařilo se uložit spawns.yml: " + e.getMessage());
        }
    }

    private void write(String path, Location l) {
        data.set(path + ".world", l.getWorld().getName());
        data.set(path + ".x", l.getX());
        data.set(path + ".y", l.getY());
        data.set(path + ".z", l.getZ());
        data.set(path + ".yaw", (double) l.getYaw());
        data.set(path + ".pitch", (double) l.getPitch());
    }

    private Location read(String path) {
        if (!data.isConfigurationSection(path)) return null;
        World world = Bukkit.getWorld(data.getString(path + ".world", ""));
        if (world == null) return null;
        return new Location(world,
                data.getDouble(path + ".x"),
                data.getDouble(path + ".y"),
                data.getDouble(path + ".z"),
                (float) data.getDouble(path + ".yaw"),
                (float) data.getDouble(path + ".pitch"));
    }

    /** Spawn pro daný svět (při per-world režimu spawn světa, jinak hlavní). */
    public Location getSpawn(World reference) {
        if (reference != null && plugin.getConfig().getBoolean("spawn.per-world", false)) {
            Location perWorld = read("worlds." + reference.getName());
            if (perWorld != null) return perWorld;
        }
        return read("spawn");
    }

    public void setSpawn(Location l) {
        write("spawn", l);
        write("worlds." + l.getWorld().getName(), l);
        save();
        if (plugin.getConfig().getBoolean("spawn.sync-world-spawn", true)) {
            l.getWorld().setSpawnLocation(l);
        }
    }

    public Location getFirstSpawn() {
        return read("first-spawn");
    }

    public void setFirstSpawn(Location l) {
        write("first-spawn", l);
        save();
    }
}
