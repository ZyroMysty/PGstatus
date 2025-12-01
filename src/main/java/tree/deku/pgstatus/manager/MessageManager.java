package tree.deku.pgstatus.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import tree.deku.pgstatus.PGstatus;

import java.util.Locale;
import java.util.Map;

public class MessageManager {

    private final PGstatus plugin;

    public MessageManager(PGstatus plugin) {
        this.plugin = plugin;
    }

    // ------------------------------------------------------------
    //  GET MESSAGE (no placeholders)
    // ------------------------------------------------------------
    public Component get(String key) {
        return buildMessage(key, null);
    }

    // ------------------------------------------------------------
    //  GET MESSAGE with placeholders
    // ------------------------------------------------------------
    public Component get(String key, Map<String, String> placeholders) {
        return buildMessage(key, placeholders);
    }

    // ------------------------------------------------------------
    //  MAIN BUILDER
    // ------------------------------------------------------------
    private Component buildMessage(String key, Map<String, String> placeholders) {

        String base = "messages." + key;

        // TEXT
        String text = plugin.getConfig().getString(base + ".text");
        if (text == null) {
            return Component.text("Missing message: " + key, NamedTextColor.RED);
        }

        // PLACEHOLDERS
        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                text = text.replace("%" + e.getKey() + "%", e.getValue());
            }
        }

        // COLOR
        String colorStr = plugin.getConfig().getString(base + ".color", "white");
        TextColor color = parseColor(colorStr);

        // PREFIX?
        boolean usePrefix = plugin.getConfig().getBoolean(base + ".prefix", false);

        if (usePrefix) {
            String pText = plugin.getConfig().getString("default-prefix.text", "[PG]");
            String pColorStr = plugin.getConfig().getString("default-prefix.color", "white");
            TextColor pColor = parseColor(pColorStr);

            return Component.text(pText + " ", pColor).append(Component.text(text, color));
        }

        return Component.text(text, color);
    }

    // ------------------------------------------------------------
    //  COLOR PARSER
    // ------------------------------------------------------------
    private TextColor parseColor(String input) {
        if (input == null) return NamedTextColor.WHITE;

        TextColor named = NamedTextColor.NAMES.value(input.toLowerCase(Locale.ROOT));
        if (named != null) return named;

        try {
            if (!input.startsWith("#")) input = "#" + input;
            return TextColor.fromHexString(input);
        } catch (Exception e) {
            return NamedTextColor.WHITE;
        }
    }

    // ------------------------------------------------------------
    //  SPECIAL CASE: STATUS SET FORMATTER
    // ------------------------------------------------------------
    public Component formatStatusSet(String prefix, String status, TextColor statusColor) {
        String open = plugin.getConfig().getString("messages.status-set-open", "[");
        String close = plugin.getConfig().getString("messages.status-set-close", "]");
        TextColor bracketColor = parseColor(plugin.getConfig().getString("messages.status-bracket-color", "gray"));

        return Component.text(prefix + " ", plugin.getStatusManager().getPrefixColor())
                .append(Component.text("Status gesetzt auf "))
                .append(Component.text(open, bracketColor))
                .append(Component.text(status, statusColor))
                .append(Component.text(close, bracketColor));
    }
}
