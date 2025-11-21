package tree.deku.pgstatus.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import tree.deku.pgstatus.BlacklistManager;
import tree.deku.pgstatus.PGstatus;
import tree.deku.pgstatus.StatusManager;

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
            sender.sendMessage("Nur Ingame, Bro.");
            return true;
        }

//        if (!player.hasPermission("statustag.use")) {
//            player.sendMessage("§cDafür hast du keine Rechte.");
//            return true;
//        }

        if (args.length == 0) {
            player.sendMessage("§7Benutzung: §e/" + label + " <text> [color]");
            return true;
        }

        String text;
        TextColor color = null;

        if (args.length == 1) {
            text = args[0];
        } else {
            text = String.join(" ", Arrays.copyOf(args, args.length - 1));
            String colorArg = args[args.length - 1];

            color = parseColor(colorArg);
            if (color == null) {
                player.sendMessage("§cUnbekannte Farbe '" + colorArg + "'. Nutze z.B.: red, green, yellow, #ff0000");
                return true;
            }
        }

        if (text.length() > 16) {
            player.sendMessage("§cDer Status ist zu lang (max. 16 Zeichen).");
            return true;
        }

        if (!blacklistManager.handleStatusAttempt(player, text)) {
            return true;
        }

        statusManager.savePlayerStatus(player.getUniqueId(), text, color);
        statusManager.applyStatus(player);

        String prefixText = statusManager.getPrefixText();
        TextColor prefixColor = statusManager.getPrefixColor();
        TextColor effectiveColor = statusManager.getColorForStatus(text);

        Component msg = Component.text(prefixText + " ", prefixColor)
                .append(Component.text("Status gesetzt auf "))
                .append(Component.text("[", NamedTextColor.GRAY))
                .append(Component.text(text, effectiveColor))
                .append(Component.text("]", NamedTextColor.GRAY));

        player.sendMessage(msg);


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
                    .toList();
        }

        if (args.length >= 2) {
            String last = args[args.length - 1].toLowerCase(Locale.ROOT);

            List<String> baseColors = Arrays.asList(
                    "red", "green", "blue", "yellow", "gold", "gray", "white", "black", "aqua", "dark_red"
            );

            return baseColors.stream()
                    .filter(c -> c.startsWith(last))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
