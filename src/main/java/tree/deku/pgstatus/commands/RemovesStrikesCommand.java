package tree.deku.pgstatus.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import tree.deku.pgstatus.manager.BlacklistManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RemovesStrikesCommand implements CommandExecutor, TabCompleter {

    private final BlacklistManager blacklistManager;

    public RemovesStrikesCommand(BlacklistManager blacklistManager) {
        this.blacklistManager = blacklistManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {

//        if(!sender.hasPermission("statustag.admin")){
//           sender.sendMessage(Component.text("Keine Rechte für diesen Command", NamedTextColor.RED));
//           return true;
//        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /removestrikes <player|all>", NamedTextColor.YELLOW));
            return true;
        }

        String targetArg = args[0];

        if (targetArg.equalsIgnoreCase("all")) {
            blacklistManager.clearAllStrikes();
            sender.sendMessage(Component.text("Alle Strikes wurden gelöscht", NamedTextColor.GREEN));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetArg);
        UUID uuid = target.getUniqueId();

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(Component.text("Diesen Spieler gibt es nicht.", NamedTextColor.RED));
            return true;
        }

        if (!blacklistManager.hasStrikes(uuid)) {
            sender.sendMessage(Component.text("Dieser Spieler hat keine Strikes.", NamedTextColor.YELLOW));
            return true;
        }

        blacklistManager.clearStrikes(uuid);

        String name = target.getName() != null ? target.getName() : targetArg;

        sender.sendMessage(Component.text("Alle Strikes von " + name + " gelöscht.", NamedTextColor.GREEN));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {

//        if (!sender.hasPermission("statustag.admin")) {
//            return List.of();
//        }

        if (args.length == 1) {
            String input = args[0].toLowerCase();

            List<String> suggestions = new ArrayList<>();
            if ("all".startsWith(input)) {
                suggestions.add("all");
            }

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
