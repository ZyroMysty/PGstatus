package tree.deku.pgstatus.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import tree.deku.pgstatus.PGstatus;
import tree.deku.pgstatus.manager.BlacklistManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CheckStrikesCommand implements CommandExecutor, TabCompleter {
    private final PGstatus plugin;
    private final BlacklistManager blacklistManager;

    public CheckStrikesCommand(BlacklistManager blacklistManager, PGstatus plugin) {
        this.blacklistManager = blacklistManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!sender.hasPermission("status.admin")) {
            sender.sendMessage(plugin.messages().get("no-permission"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(plugin.messages().get("checkstrikes-usage"));
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("all")) {

            int page = 1;

            if (args.length == 2) {
                try {
                    page = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(plugin.messages().get("blacklist-invalid-page"));
                    return true;
                }
            }

            listAllStrikes(sender, page);
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        UUID uuid = target.getUniqueId();
        String name = target.getName() != null ? target.getName() : args[0];

        int strikes = blacklistManager.getStrikeCount(uuid);

        if (strikes == 0) {
            sender.sendMessage(plugin.messages().get("checkstrikes-none",
                    Map.of("player", name)));
            return true;
        }

        String plural = strikes == 1 ? "" : "s";

        sender.sendMessage(plugin.messages().get("checkstrikes-some",
                Map.of(
                        "player", name,
                        "count", String.valueOf(strikes),
                        "plural", plural
                )));

        return true;

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {

        if (!sender.hasPermission("status.admin")) return List.of();

        if (args.length == 1) {
            String input = args[0].toLowerCase();

            List<String> suggestions = new ArrayList<>();
            suggestions.add("all");

            Bukkit.getOnlinePlayers().forEach(p -> {
                String name = p.getName();
                if (name.toLowerCase().startsWith(input)) {
                    suggestions.add(name);
                }
            });

            return suggestions;
        }

        return List.of();
    }

    private void listAllStrikes(CommandSender sender, int page) {

        Map<UUID, Integer> strikeMap = blacklistManager.getAllStrikeEntries();

        if (strikeMap.isEmpty()) {
            sender.sendMessage(plugin.messages().get("checkstrikes-list-none"));
            return;
        }

        List<Map.Entry<UUID, Integer>> entries = new ArrayList<>(strikeMap.entrySet());
        entries.sort((a, b) -> b.getValue() - a.getValue());

        int pageSize = 10;
        int totalPages = (int) Math.ceil(entries.size() / (double) pageSize);

        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, entries.size());

        List<Map.Entry<UUID, Integer>> pageEntries = entries.subList(start, end);

        // HEADER
        sender.sendMessage(plugin.messages().get("checkstrikes-list-header",
                Map.of("count", String.valueOf(entries.size()))));

        // PAGE X/Y
        sender.sendMessage(plugin.messages().get("checkstrikes-list-page",
                Map.of(
                        "page", String.valueOf(page),
                        "pages", String.valueOf(totalPages)
                )));

        // ENTRIES
        for (Map.Entry<UUID, Integer> entryData : pageEntries) {
            UUID id = entryData.getKey();
            int count = entryData.getValue();
            String playerName = Bukkit.getOfflinePlayer(id).getName();
            if (playerName == null) playerName = id.toString();

            String plural = (count == 1) ? " Strike" : " Strikes";

            // config values
            String bullet = plugin.messages().raw("checkstrikes-list-entry.bullet", "-");
            NamedTextColor bulletColor = plugin.messages().color("checkstrikes-list-entry.bullet-color", NamedTextColor.DARK_GRAY);
            NamedTextColor playerColor = plugin.messages().color("checkstrikes-list-entry.player-color", NamedTextColor.GOLD);

            // build final component
            Component line = Component.text(bullet + " ", bulletColor)
                    .append(Component.text(playerName + ": ", playerColor))
                    .append(
                            plugin.messages().get("checkstrikes-list-entry",
                                    Map.of(
                                            "player", playerName,
                                            "count", String.valueOf(count),
                                            "plural", plural
                                    )
                            )
                    );

            sender.sendMessage(line);
        }


        // NAVIGATION
        Component nav = Component.text("");

        if (page > 1) {
            nav = nav.append(
                    plugin.messages().get("checkstrikes-list-prev")
                            .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/checkstrikes all " + (page - 1)))
            );
        }

        if (page < totalPages) {
            nav = nav.append(Component.text(" "))
                    .append(
                            plugin.messages().get("checkstrikes-list-next")
                                    .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/checkstrikes all " + (page + 1)))
                    );
        }

        sender.sendMessage(nav);

        // FOOTER
        sender.sendMessage(plugin.messages().get("checkstrikes-list-footer"));
    }


}
