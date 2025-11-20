package tree.deku.pgstatus;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class StatusManager {

    private final PGstatus plugin;

    private final Map<UUID, String> statusByPlayer = new HashMap<>();
    private final Map<String, TextColor> colorByStatus = new HashMap<>();

    //default colors/texts
    private final TextColor defaultColor;
    private final String prefixText;
    private final TextColor prefixColor;


    public StatusManager(PGstatus plugin) {
        this.plugin = plugin;

        this.defaultColor = parseColorFromConfig(plugin.getConfig().getString("default-color"), NamedTextColor.GOLD);
        this.prefixText = plugin.getConfig().getString("default-prefix.text", "[PG]");

        this.prefixColor = parseColorFromConfig(plugin.getConfig().getString("default-prefix.color"), NamedTextColor.LIGHT_PURPLE);
        loadFromConfig();
    }

    private void loadFromConfig() {
        statusByPlayer.clear();
        colorByStatus.clear();

        ConfigurationSection statusSection = plugin.getConfig().getConfigurationSection("statuses");
        if (statusSection != null) {
            for (String key : statusSection.getKeys(false)) {
                String colorHex = statusSection.getString(key);
                TextColor color = parseColorFromConfig(colorHex, defaultColor);
                colorByStatus.put(key, color);
            }
        }

        ConfigurationSection playersSection = plugin.getConfig().getConfigurationSection("players");
        if (playersSection != null) {
            for (String key : playersSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    String text = playersSection.getString(key + ".text");
                    if (text != null && !text.isEmpty()) {
                        statusByPlayer.put(uuid, text);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }


    private TextColor parseColorFromConfig(String value, TextColor fallback) {
        if (value == null || value.isEmpty()) {
            return fallback;
        }

        String lower = value.toLowerCase(Locale.ROOT);

        // 1. Named color (z.B. "gold")
        TextColor named = NamedTextColor.NAMES.value(lower);
        if (named != null) {
            return named;
        }

        // 2. Hex (#ff0000 oder ff0000)
        if (!lower.startsWith("#")) {
            lower = "#" + lower;
        }

        try {
            return TextColor.fromHexString(lower);
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    public TextColor getColorForStatus(String statusText) {
        TextColor c = colorByStatus.get(statusText);
        return c != null ? c : defaultColor;
    }


    public void savePlayerStatus(UUID uuid, String text, TextColor newColor) {

        statusByPlayer.put(uuid, text);

        // wenn neue Farbe angegeben -> globale Farbe für diesen Status setzen
        if (newColor != null) {
            colorByStatus.put(text, newColor);
        } else if (!colorByStatus.containsKey(text)) {
            // Status noch nie gesehen -> Default-Farbe nutzen
            colorByStatus.put(text, defaultColor);
        }

        saveAllToConfig();

        // Alle Online-Spieler, die diesen Status haben, updaten
        reapplyStatusForText(text);
    }


    public void clearPlayerStatus(UUID uuid) {

        String removedStatus = statusByPlayer.remove(uuid);

        plugin.getConfig().set("players." + uuid, null);
        plugin.saveConfig();

        if (removedStatus != null && !statusByPlayer.containsValue(removedStatus)) {
            colorByStatus.remove(removedStatus);
            plugin.getConfig().set("statuses." + removedStatus, null);
            plugin.saveConfig();
        }
    }


    private void saveAllToConfig() {
        // erstmal alles bereinigen
        plugin.getConfig().set("statuses", null);
        plugin.getConfig().set("players", null);

        // statuses speichern
        for (Map.Entry<String, TextColor> entry : colorByStatus.entrySet()) {
            plugin.getConfig().set("statuses." + entry.getKey(), entry.getValue().asHexString());
        }

        // players speichern
        for (Map.Entry<UUID, String> entry : statusByPlayer.entrySet()) {
            String path = "players." + entry.getKey();
            plugin.getConfig().set(path + ".text", entry.getValue());
        }

        plugin.saveConfig();
    }

    private void reapplyStatusForText(String text) {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            UUID id = p.getUniqueId();
            String playerStatus = statusByPlayer.get(id);
            if (text.equals(playerStatus)) {
                applyStatus(p);
            }
        }
    }

    public void applyStatus(Player player) {
        UUID uuid = player.getUniqueId();
        String status = statusByPlayer.get(uuid);

        if (status == null || status.isEmpty()) {
            resetPlayerListName(player);
            return;
        }

        TextColor color = getColorForStatus(status);

        Component prefix = Component.text("[")
                .append(Component.text(status, color).decoration(TextDecoration.BOLD, true))
                .append(Component.text("] "));

        Component name = Component.text(player.getName(), NamedTextColor.WHITE);

        player.playerListName(prefix.append(name));
    }


    public void resetPlayerListName(Player player) {
        player.playerListName(null);
    }

    public List<String> getAllStatusTextsSorted() {
        Set<String> set = new HashSet<>(statusByPlayer.values());
        List<String> list = new ArrayList<>(set);
        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
        return list;
    }

    public String getPrefixText() {
        return prefixText;
    }

    public TextColor getPrefixColor() {
        return prefixColor;
    }
}