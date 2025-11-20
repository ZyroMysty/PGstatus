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

    public StatusManager(PGstatus plugin) {
        this.plugin = plugin;
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

    public void savePlayerStatus(UUID uuid, String text, TextColor color) {
        statusByPlayer.put(uuid, text);
        if (color != null) {
            colorByPlayer.put(uuid, color);
        } else {
            colorByPlayer.remove(uuid);
        }

        String path = "players." + uuid;
        plugin.getConfig().set(path + ".text", text);
        plugin.getConfig().set(path + ".color", color != null ? color.asHexString() : null);
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

        Component prefix = Component.text("[")
                .append(Component.text(status, color).decoration(TextDecoration.BOLD, true))
                .append(Component.text("] "));

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
}