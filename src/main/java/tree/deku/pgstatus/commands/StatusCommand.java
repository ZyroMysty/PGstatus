package tree.deku.pgstatus.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import tree.deku.pgstatus.manager.BlacklistManager;
import tree.deku.pgstatus.PGstatus;
import tree.deku.pgstatus.manager.StatusManager;

import java.util.*;
import java.util.stream.Collectors;

public class StatusCommand implements CommandExecutor, TabCompleter {

    private final PGstatus plugin;
    private final StatusManager statusManager;
    private final BlacklistManager blacklistManager;

    public StatusCommand(PGstatus plugin, StatusManager statusManager, BlacklistManager blacklistManager) {
        this.plugin = plugin;
        this.statusManager = statusManager;
        this.blacklistManager = blacklistManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Command not available through console.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(plugin.messages().get("usage-status"));
            return true;
        }

        String text;
        TextColor color = null;

        String lastArg = args[args.length - 1];
        TextColor parsedColor = parseColor(lastArg);

        if (parsedColor != null && args.length > 1) {
            // Letztes Argument ist eine gültige Farbe
            color = parsedColor;
            text = String.join(" ", Arrays.copyOf(args, args.length - 1));
        } else {
            // Keine Farbe oder ungültiges Argument → alles ist Text
            text = String.join(" ", args);
        }

        text = text.trim();
        if (text.isEmpty()) {
            player.sendMessage(plugin.messages().get("status-empty"));
            return true;
        }

        if (text.length() > 16) {
            player.sendMessage(plugin.messages().get("status-too-long"));
            return true;
        }

        // ---- protected statuses check ----
        String normalized = text.toUpperCase(Locale.ROOT);

        List<String> protectedList = plugin.getConfig().getStringList("protected-statuses");
        boolean isProtected = protectedList.stream()
                .map(s -> s.toUpperCase(Locale.ROOT))
                .anyMatch(s -> s.equals(normalized));

        if (isProtected) {
            String permPrefix = plugin.getConfig().getString("protected-status-permission-prefix", "status.status.");
            String perm = permPrefix + normalized; // z.B. status.status.DEV

            if (!player.hasPermission(perm)) {
                player.sendMessage(plugin.messages().get("status-protected-deny",
                        Map.of("status", normalized)));
                return true;
            }
        }

        if (!blacklistManager.handleStatusAttempt(player, text)) {
            return true;
        }

        statusManager.savePlayerStatus(player.getUniqueId(), text, color);
        statusManager.applyStatus(player);

        TextColor effectiveColor = statusManager.getColorForStatus(text);

        player.sendMessage(
                plugin.messages().formatStatusSet(
                        statusManager.getPrefixText(),
                        text,
                        effectiveColor
                )
        );


        return true;
    }

    private TextColor parseColor(String input) {
        TextColor named = NamedTextColor.NAMES.value(input.toLowerCase(Locale.ROOT));
        if (named != null) return named;

        if (!input.startsWith("#")) {
            input = "#" + input;
        }
        try {
            return TextColor.fromHexString(input);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if (!(sender instanceof Player)) return Collections.emptyList();

        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);

            List<String> allStatuses = statusManager.getAllStatusTextsSorted();

            // wenn noch keiner was gesetzt hat → keine Vorschläge
            if (allStatuses.isEmpty()) {
                return Collections.emptyList();
            }

            return allStatuses.stream()
                    .filter(s -> s.toLowerCase(Locale.ROOT).startsWith(prefix))
                    .filter(s -> canUseStatus((Player) sender, s))
                    .toList();
        }

        if (args.length >= 2) {
            String last = args[args.length - 1].toLowerCase(Locale.ROOT);

            List<String> baseColors = Arrays.asList(
                    "red", "green", "blue", "yellow", "gold", "gray", "white", "black", "aqua", "dark_red", "dark_green", "dark_gray", "dark_blue", "dark_aqua", "dark_purple", "light_purple"
            );

            return baseColors.stream()
                    .filter(c -> c.startsWith(last))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    private boolean canUseStatus(Player player, String statusText) {
        String normalized = statusText.toUpperCase(Locale.ROOT);

        List<String> protectedList = plugin.getConfig().getStringList("protected-statuses");
        boolean isProtected = protectedList.stream()
                .map(s -> s.toUpperCase(Locale.ROOT))
                .anyMatch(s -> s.equals(normalized));

        if (!isProtected) return true;

        String permPrefix = plugin.getConfig().getString("protected-status-permission-prefix", "status.status.");
        return player.hasPermission(permPrefix + normalized);
    }

}
