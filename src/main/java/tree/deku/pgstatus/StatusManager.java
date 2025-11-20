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
    private final Map<UUID, TextColor> colorByPlayer = new HashMap<>();

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
        colorByPlayer.clear();

        ConfigurationSection playersSection = plugin.getConfig().getConfigurationSection("players");
        if (playersSection == null) return;

        for (String key : playersSection.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String text = playersSection.getString(key + ".text");
                String colorHex = playersSection.getString(key + ".color");

                if (text != null && !text.isEmpty()) {
                    statusByPlayer.put(uuid, text);
                }
                if (colorHex != null && !colorHex.isEmpty()) {
                    try {
                        TextColor color = TextColor.fromHexString(colorHex);
                        colorByPlayer.put(uuid, color);
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            } catch (IllegalArgumentException ignored) {

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


    public void savePlayerStatus(UUID uuid, String text, TextColor color) {
        if (color == null) {
            color = defaultColor;
        }

        statusByPlayer.put(uuid, text);
        colorByPlayer.put(uuid, color);

        String path = "players." + uuid;
        plugin.getConfig().set(path + ".text", text);
        plugin.getConfig().set(path + ".color", color.asHexString());
        plugin.saveConfig();
    }

    public void clearPlayerStatus(UUID uuid) {
        statusByPlayer.remove(uuid);
        colorByPlayer.remove(uuid);

        plugin.getConfig().set("players." + uuid, null);
        plugin.saveConfig();
    }

    public String getStatus(UUID uuid) {
        return statusByPlayer.get(uuid);
    }

    public TextColor getColor(UUID uuid) {
        return colorByPlayer.get(uuid);
    }

    public void applyStatus(Player player) {
        String status = getStatus(player.getUniqueId());
        if (status == null || status.isEmpty()) {
            resetPlayerListName(player);
            return;
        }

        TextColor color = getColor(player.getUniqueId());
        if (color == null) {
            color = NamedTextColor.GOLD;
        }

        Component prefix = Component.text("[").append(Component.text(status, color).decoration(TextDecoration.BOLD, true)).append(Component.text("] "));

        Component name = Component.text(player.getName(), NamedTextColor.WHITE);

        Component full = prefix.append(name);

        player.playerListName(full);
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

    public TextColor getStatusColor(UUID uuid) {
        return colorByPlayer.get(uuid);
    }

}