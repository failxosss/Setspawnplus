package cz.setspawnplus;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class SetSpawnPlugin extends JavaPlugin {

    private Messages messages;
    private SpawnManager spawns;
    private TeleportService teleports;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.messages = new Messages(this);
        this.spawns = new SpawnManager(this);
        this.teleports = new TeleportService(this);

        SpawnCommands commands = new SpawnCommands(this);
        for (String name : new String[]{"spawn", "setspawn", "firstspawn", "setfirstspawn", "setspawnplus"}) {
            PluginCommand cmd = getCommand(name);
            if (cmd != null) {
                cmd.setExecutor(commands);
                cmd.setTabCompleter(commands);
            }
        }
        getServer().getPluginManager().registerEvents(new SpawnListener(this), this);
        getLogger().info("SetSpawnPlus zapnut.");
    }

    @Override
    public void onDisable() {
        if (teleports != null) teleports.cancelAll();
    }

    /** Znovu načte config.yml i spawns.yml. */
    public void reloadAll() {
        reloadConfig();
        spawns.load();
    }

    public Messages messages() { return messages; }
    public SpawnManager spawns() { return spawns; }
    public TeleportService teleports() { return teleports; }
}
