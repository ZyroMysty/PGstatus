package tree.deku.pgstatus.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import tree.deku.pgstatus.manager.BlacklistManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CheckStrikesCommand implements CommandExecutor, TabCompleter {

    private final BlacklistManager blacklistManager;

    public CheckStrikesCommand(BlacklistManager blacklistManager) {
        this.blacklistManager = blacklistManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

//        if (!sender.hasPermission("statustag.admin")) {
//            sender.sendMessage(Component.text("Keine Rechte", NamedTextColor.RED));
//            return true;
//        }

        if (args.length != 1) {
            sender.sendMessage(Component.text("Usage: /checkstrikes <player>", NamedTextColor.YELLOW));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        UUID uuid = target.getUniqueId();

        int strikes = blacklistManager.getStrikeCount(uuid);

        if (strikes == 0) {
            sender.sendMessage(Component.text((target.getName() != null ? target.getName() : args[0]) + " hat keine Strikes.", NamedTextColor.GREEN));
            return true;
        }

        sender.sendMessage(Component.text((target.getName() != null ? target.getName() : args[0]) + " hat aktuell " + strikes + " Strike" + (strikes == 1 ? "" : "s") + ".", NamedTextColor.YELLOW));
        return true;

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {

        if (!sender.hasPermission("statustag.admin")) return List.of();

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
