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

}
