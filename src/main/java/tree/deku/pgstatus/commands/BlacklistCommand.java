package tree.deku.pgstatus.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import tree.deku.pgstatus.PGstatus;
import tree.deku.pgstatus.manager.BlacklistManager;
import tree.deku.pgstatus.utils.Pagination;

import java.util.*;

public class BlacklistCommand implements CommandExecutor, TabCompleter {

    private final PGstatus plugin;
    private final BlacklistManager blacklistManager;

    public BlacklistCommand(BlacklistManager blacklistManager, PGstatus plugin){
        this.blacklistManager = blacklistManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if(!commandSender.hasPermission("status.admin")){
            commandSender.sendMessage(plugin.messages().get("no-permission"));
            return true;
        }

        if(args.length == 0){
            commandSender.sendMessage(plugin.messages().get("blacklist-usage"));
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
                        commandSender.sendMessage(plugin.messages().get("blacklist-invalid-page"));
                        return true;
                    }
                }

                handleList(commandSender, page);
            }
            case "add" -> handleAdd(commandSender, args);
            case "remove" -> handleRemove(commandSender, args);
            case "reload" -> handleReload(commandSender, args);
            default -> commandSender.sendMessage(plugin.messages().get("blacklist-usage"));
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
        sender.sendMessage(
                plugin.messages().get("blacklist-header", Map.of("count", String.valueOf(words.size())))
        );


        // Titelzeile
        sender.sendMessage(
                plugin.messages().get("blacklist-page", Map.of(
                        "page", String.valueOf(page),
                        "pages", String.valueOf(totalPages)
                ))
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
                    plugin.messages().get("blacklist-prev")
                            .clickEvent(ClickEvent.runCommand("/blacklist list " + (page - 1)))
                            .hoverEvent(Component.text("Zur Seite " + (page - 1)))
            );

        }

        if (page < totalPages) {
            // Next >>
            nav = nav.append(
                    plugin.messages().get("blacklist-next")
                            .clickEvent(ClickEvent.runCommand("/blacklist list " + (page + 1)))
                            .hoverEvent(Component.text("Zur Seite " + (page + 1)))
            );

        }

        sender.sendMessage(nav);

        // Rahmen unten
        sender.sendMessage(plugin.messages().get("blacklist-footer"));

    }



    private void handleAdd(CommandSender sender, String[] args) {
        if (args.length < 2) {
            usage(sender, "/blacklist add <wort>");
            return;
        }

        String word = args[1];
        boolean added = blacklistManager.addWord(word);

        if (added)
            sender.sendMessage(plugin.messages().get("blacklist-add-success", Map.of("word", word)));
        else
            sender.sendMessage(plugin.messages().get("blacklist-add-fail"));

    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.messages().get("blacklist-usage-remove"));
            return;
        }

        String word = args[1];
        boolean removed = blacklistManager.removeWord(word);

        if (removed) {
            sender.sendMessage(plugin.messages().get("blacklist-remove-success",
                    Map.of("word", word)
            ));
        } else {
            sender.sendMessage(plugin.messages().get("blacklist-remove-fail",
                    Map.of("word", word)
            ));
        }
    }

    private void handleReload(CommandSender sender, String[] args){
        boolean success = blacklistManager.reload();
        sender.sendMessage(plugin.messages().get(success ? "blacklist-reload-success" : "blacklist-reload-fail"));

    }

    private boolean usage(CommandSender sender, String u) {
        msg(sender, "Usage: " + u, NamedTextColor.YELLOW);
        return true;
    }

    private void msg(CommandSender sender, String text, NamedTextColor color) {
        sender.sendMessage(Component.text(text, color));
    }

}
