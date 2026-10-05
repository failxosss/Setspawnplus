package cz.setspawnplus;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class SpawnCommands implements CommandExecutor, TabCompleter {

    private final SetSpawnPlugin plugin;

    public SpawnCommands(SetSpawnPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Messages msg = plugin.messages();

        switch (command.getName().toLowerCase()) {
            case "spawn" -> {
                if (args.length >= 1) {
                    if (!sender.hasPermission("setspawn.use.others")) {
                        msg.send(sender, "no-permission");
                        return true;
                    }
                    Player target = Bukkit.getPlayerExact(args[0]);
                    if (target == null) {
                        msg.send(sender, "player-not-found", "%player%", args[0]);
                        return true;
                    }
                    Location spawn = plugin.spawns().getSpawn(target.getWorld());
                    if (spawn == null) {
                        msg.send(sender, "spawn-not-set");
                        return true;
                    }
                    plugin.teleports().teleport(target, spawn, true);
                    msg.send(sender, "teleported-other", "%player%", target.getName());
                    return true;
                }
                if (!(sender instanceof Player player)) {
                    msg.send(sender, "players-only");
                    return true;
                }
                if (!player.hasPermission("setspawn.use")) {
                    msg.send(sender, "no-permission");
                    return true;
                }
                Location spawn = plugin.spawns().getSpawn(player.getWorld());
                if (spawn == null) {
                    msg.send(sender, "spawn-not-set");
                    return true;
                }
                plugin.teleports().teleport(player, spawn, false);
            }
            case "setspawn" -> {
                if (!(sender instanceof Player player)) {
                    msg.send(sender, "players-only");
                    return true;
                }
                if (!player.hasPermission("setspawn.set")) {
                    msg.send(sender, "no-permission");
                    return true;
                }
                plugin.spawns().setSpawn(player.getLocation());
                msg.send(sender, "spawn-set");
            }
            case "firstspawn" -> {
                if (!(sender instanceof Player player)) {
                    msg.send(sender, "players-only");
                    return true;
                }
                if (!player.hasPermission("setspawn.firstspawn.use")) {
                    msg.send(sender, "no-permission");
                    return true;
                }
                Location spawn = plugin.spawns().getFirstSpawn();
                if (spawn == null) {
                    msg.send(sender, "firstspawn-not-set");
                    return true;
                }
                plugin.teleports().teleport(player, spawn, false);
            }
            case "setfirstspawn" -> {
                if (!(sender instanceof Player player)) {
                    msg.send(sender, "players-only");
                    return true;
                }
                if (!player.hasPermission("setspawn.firstspawn.set")) {
                    msg.send(sender, "no-permission");
                    return true;
                }
                plugin.spawns().setFirstSpawn(player.getLocation());
                msg.send(sender, "firstspawn-set");
            }
            case "setspawnplus" -> {
                if (args.length == 0) {
                    msg.send(sender, "usage");
                    return true;
                }
                if (args[0].equalsIgnoreCase("reload")) {
                    if (!sender.hasPermission("setspawn.reload")) {
                        msg.send(sender, "no-permission");
                        return true;
                    }
                    plugin.reloadAll();
                    msg.send(sender, "reloaded");
                } else if (args[0].equalsIgnoreCase("info")) {
                    sender.sendMessage(Messages.color("&bSetSpawnPlus &7v" + plugin.getDescription().getVersion()
                            + " &8| &7spawn: " + (plugin.spawns().getSpawn(null) != null ? "&anastaven" : "&cnenastaven")
                            + " &8| &7firstspawn: " + (plugin.spawns().getFirstSpawn() != null ? "&anastaven" : "&cnenastaven")));
                } else {
                    msg.send(sender, "usage");
                }
            }
            default -> { return false; }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        String name = command.getName().toLowerCase();
        if (name.equals("spawn") && args.length == 1 && sender.hasPermission("setspawn.use.others")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) out.add(p.getName());
            }
        } else if (name.equals("setspawnplus") && args.length == 1) {
            for (String s : new String[]{"reload", "info"}) {
                if (s.startsWith(args[0].toLowerCase())) out.add(s);
            }
        }
        return out;
    }
}
