package tree.deku.pgstatus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BlacklistManager {

    private final PGstatus plugin;

    private final boolean enabled;
    private final int maxStrikes;

    private final Set<String> blacklistWords;
    private final Map<UUID, Integer> strikes = new HashMap<>();

    private final File blacklistFile;
    private FileConfiguration blacklistConfig;


    public BlacklistManager(PGstatus plugin) {
        this.plugin = plugin;

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("blacklist");
        if (section == null) {
            enabled = false;
            maxStrikes = 3;
        } else {
            enabled = section.getBoolean("enabled", true);
            maxStrikes = section.getInt("max-strikes", 3);
            loadStrikesFromConfig(section);

        }

        blacklistFile = new File(plugin.getDataFolder(), "blacklist.yml");
        loadOrCreateBlacklistFile();

        List<String> words = blacklistConfig.getStringList("words");
        blacklistWords = words.stream().filter(Objects::nonNull).map(String::toLowerCase).collect(Collectors.toSet());
    }

    private void loadOrCreateBlacklistFile() {
        if (!blacklistFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                blacklistFile.createNewFile();

                blacklistConfig = YamlConfiguration.loadConfiguration(blacklistFile);
                blacklistConfig.set("words", new ArrayList<>());
                blacklistConfig.save(blacklistFile);
            } catch (IOException e) {
                e.printStackTrace();
                blacklistConfig = YamlConfiguration.loadConfiguration(blacklistFile);
            }
        } else {
            blacklistConfig = YamlConfiguration.loadConfiguration(blacklistFile);
        }
    }

    private void loadStrikesFromConfig(ConfigurationSection section) {
        ConfigurationSection strikeSection = section.getConfigurationSection("strikes");
        if (strikeSection == null) return;

        for (String key : strikeSection.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                int count = strikeSection.getInt(key, 0);
                if (count > 0) {
                    strikes.put(uuid, count);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }

    }

    private void saveStrikesToConfig() {
        ConfigurationSection blacklistSection = plugin.getConfig().getConfigurationSection("blacklist");
        if (blacklistSection == null) {
            blacklistSection = plugin.getConfig().createSection("blacklist");
        }

        if (blacklistSection.isConfigurationSection("strikes")) {
            blacklistSection.set("strikes", null);
        }

        ConfigurationSection strikesSection = blacklistSection.createSection("strikes");

        for (Map.Entry<UUID, Integer> entry : strikes.entrySet()) {
            strikesSection.set(entry.getKey().toString(), entry.getValue());
        }

        plugin.saveConfig();
    }


    public boolean isEnabled() {
        return enabled && !blacklistWords.isEmpty();
    }

    private boolean isBlacklisted(String text) {
        if (!isEnabled()) return false;
        String lower = text.toLowerCase(Locale.ROOT);
        for (String bad : blacklistWords) {
            if (lower.contains(bad)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true = Status darf gesetzt werden
     * false = Status wird blockiert
     */
    public boolean handleStatusAttempt(Player player, String statusText) {
        if (!isBlacklisted(statusText)) {
            return true;
        }

        UUID uuid = player.getUniqueId();
        int current = strikes.getOrDefault(uuid, 0) + 1;
        strikes.put(uuid, current);
        saveStrikesToConfig();

        if (current < maxStrikes) {
            player.sendMessage(Component.text("Dieser Status ist nicht erlaubt. Verwarnung " + current + "/" + maxStrikes,
                    NamedTextColor.RED));
            return false;
        }

        if (current == maxStrikes) {
            Bukkit.getScheduler().runTask(plugin, () -> player.kick(Component.text("Zu viele verbotene Status-Versuche.", NamedTextColor.RED)));
            return false;
        }

        Bukkit.getBanList(BanList.Type.NAME).addBan(
                player.getName(),
                "Wiederholte Nutzung verbotener Status-Texte",
                null,
                plugin.getName()
        );

        Bukkit.getScheduler().runTask(plugin, () ->
                player.kick(Component.text("Du wurdest gebannt (Blacklist-Verstoß).", NamedTextColor.RED))
        );

        return false;
    }

    public void clearStrikes(UUID uuid) {
        strikes.remove(uuid);
        saveStrikesToConfig();
    }

    public void clearAllStrikes() {
        strikes.clear();
        saveStrikesToConfig();
    }

    public boolean hasStrikes(UUID uuid) {
        return strikes.containsKey(uuid) && strikes.get(uuid) > 0;
    }

    public int getStrikeCount(UUID uuid) {
        return strikes.getOrDefault(uuid, 0);
    }


    public void saveWords(){
        blacklistConfig.set("words", new ArrayList<>(blacklistWords));
        try{
            blacklistConfig.save(blacklistFile);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public boolean addWord(String word){
        String w = word.toLowerCase(Locale.ROOT);

        if(blacklistWords.contains(w))return false;

        blacklistWords.add(w);
        saveWords();
        return true;
    }

    public boolean removeWord(String word){
        String w = word.toLowerCase(Locale.ROOT);

        if(!blacklistWords.contains(w)) return false;

        blacklistWords.remove(w);
        saveWords();
        return true;
    }

    public Set<String> getWords(){
        return Collections.unmodifiableSet(blacklistWords);
    }

}
