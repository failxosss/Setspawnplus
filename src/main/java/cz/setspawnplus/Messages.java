package cz.setspawnplus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

public final class Messages {

    private final SetSpawnPlugin plugin;

    public Messages(SetSpawnPlugin plugin) {
        this.plugin = plugin;
    }

    /** Odešle zprávu z configu. Placeholdery se předávají jako dvojice klíč, hodnota. */
    public void send(CommandSender to, String key, String... placeholders) {
        String raw = plugin.getConfig().getString("messages." + key);
        if (raw == null) raw = "&cChybí zpráva: " + key;
        if (raw.isEmpty()) return;
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            raw = raw.replace(placeholders[i], placeholders[i + 1]);
        }
        to.sendMessage(color(plugin.getConfig().getString("messages.prefix", "") + raw));
    }

    public static Component color(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }
}
