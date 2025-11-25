package tree.deku.pgstatus.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import tree.deku.pgstatus.manager.BlacklistManager;
import tree.deku.pgstatus.utils.Pagination;

import java.util.ArrayList;
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
            case "list" -> {
                int page = 1;

                if (args.length >= 2) {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        commandSender.sendMessage(Component.text("Bitte gib eine gültige Seitenzahl an.", NamedTextColor.RED));
                        return true;
                    }
                }

                handleList(commandSender, page);
            }
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

    private void handleList(CommandSender sender, int page) {
        List<String> words = new ArrayList<>(blacklistManager.getWords());
        words.sort(String::compareToIgnoreCase);

        int pageSize = 10;

        int totalPages = Pagination.getTotalPages(words.size(), pageSize);

        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        List<String> pageWords = Pagination.getPage(words, page, pageSize);

        // Rahmen oben
        sender.sendMessage(Component.text("§8§m--------------- §6 Blacklist ("+words.size()+") §8§m------------"));

        // Titelzeile
        sender.sendMessage(
                Component.text("Seite ", NamedTextColor.GRAY)
                        .append(Component.text(page + "/" + totalPages, NamedTextColor.GOLD))
        );

        // Wörter
        for (String w : pageWords) {
            sender.sendMessage(
                    Component.text(" - ", NamedTextColor.DARK_GRAY)
                            .append(Component.text(w, NamedTextColor.WHITE))
            );
        }

        // Navigation-Buttons
        Component nav = Component.text("");

        if (page > 1) {
            // << Previous
            nav = nav.append(
                    Component.text("« Vorherige ", NamedTextColor.YELLOW)
                            .clickEvent(ClickEvent.runCommand("/blacklist list " + (page - 1)))
                            .hoverEvent(Component.text("Zur Seite " + (page - 1)))
            );
        }

        if (page < totalPages) {
            // Next >>
            nav = nav.append(
                    Component.text(" Nächste »", NamedTextColor.YELLOW)
                            .clickEvent(ClickEvent.runCommand("/blacklist list " + (page + 1)))
                            .hoverEvent(Component.text("Zur Seite " + (page + 1)))
            );
        }

        sender.sendMessage(nav);

        // Rahmen unten
        sender.sendMessage(Component.text("§8§m----------------------------------------"));
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
