package tree.deku.pgstatus.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tree.deku.pgstatus.BlacklistManager;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public class BlacklistCommand implements CommandExecutor, TabCompleter {

    private final BlacklistManager blacklistManager;

    public BlacklistCommand(BlacklistManager blacklistManager){
        this.blacklistManager = blacklistManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if(!commandSender.hasPermission("status.admin")){
            commandSender.sendMessage(Component.text("Keine Rechte", NamedTextColor.RED));
            return true;
        }

        if(args.length == 0){
            commandSender.sendMessage(Component.text("Usage: /blacklist <add|remove|list|reload> [wort]", NamedTextColor.YELLOW));
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        switch (sub) {
            case "list" -> handleList(commandSender);
            case "add" -> handleAdd(commandSender, args);
            case "remove" -> handleRemove(commandSender, args);
            case "reload" -> handleReload(commandSender, args);
            default -> usage(commandSender, "/blacklist <add|remove|list|reload> [wort]");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {

        if (!sender.hasPermission("statustag.admin")) return List.of();

        if (args.length == 1) {
            return List.of("add", "remove", "list", "reload").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("remove")) {
                return blacklistManager.getWords().stream()
                        .filter(w -> w.startsWith(args[1].toLowerCase()))
                        .toList();
            }
        }

        return List.of();
    }

    private void handleList(CommandSender sender) {
        Set<String> words = blacklistManager.getWords();

        if (words.isEmpty()) {
            msg(sender, "Blacklist ist leer.", NamedTextColor.GREEN);
            return;
        }

        msg(sender, "Blacklist:", NamedTextColor.YELLOW);
        words.forEach(w -> msg(sender, "- " + w, NamedTextColor.GRAY));
    }

    private void handleAdd(CommandSender sender, String[] args) {
        if (args.length < 2) {
            usage(sender, "/blacklist add <wort>");
            return;
        }

        String word = args[1];
        boolean added = blacklistManager.addWord(word);

        msg(sender,
                added ? "Hinzugefügt: " + word : "Wort ist bereits in der Blacklist.",
                added ? NamedTextColor.GREEN : NamedTextColor.RED
        );
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            usage(sender, "/blacklist remove <wort>");
            return;
        }

        String word = args[1];
        boolean removed = blacklistManager.removeWord(word);

        msg(sender,
                removed ? "Entfernt: " + word : "Wort ist nicht in der Blacklist.",
                removed ? NamedTextColor.GREEN : NamedTextColor.RED
        );
    }

    private void handleReload(CommandSender sender, String[] args){
        boolean success = blacklistManager.reload();
        if(success)msg(sender, "Blacklist reloaded", NamedTextColor.GREEN);
        else msg(sender, "Fehler beim Reload der Blacklist", NamedTextColor.RED);
    }

    private boolean usage(CommandSender sender, String u) {
        msg(sender, "Usage: " + u, NamedTextColor.YELLOW);
        return true;
    }

    private void msg(CommandSender sender, String text, NamedTextColor color) {
        sender.sendMessage(Component.text(text, color));
    }

}
