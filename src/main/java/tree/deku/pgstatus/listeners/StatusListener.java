package tree.deku.pgstatus.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import tree.deku.pgstatus.PGstatus;
import tree.deku.pgstatus.manager.StatusManager;

public class StatusListener implements Listener {
    private final StatusManager statusManager;
    private final PGstatus plugin;
    private final String currentStatusText;
    private final TextColor currentStatusColor;

    public StatusListener(PGstatus plugin, StatusManager statusManager) {
        this.plugin = plugin;
        this.currentStatusText = plugin.getConfig().getString("messages.default-currentStatus.text", "Dein aktueller Status:");
        this.currentStatusColor = statusManager.parseColorFromConfig(plugin.getConfig().getString("messages.default-currentStatus.color", "light_purple"), NamedTextColor.LIGHT_PURPLE);

        this.statusManager = statusManager;
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        String prefixText = statusManager.getPrefixText();
        TextColor prefixColor = statusManager.getPrefixColor();

        Player p = event.getPlayer();
        statusManager.applyStatus(p);

        String currentStatus = statusManager.getCurrentStatus(p.getUniqueId());
        TextColor effectiveColor = statusManager.getColorForStatus(currentStatus);

        Component msg = Component.text(prefixText + " ", prefixColor)
                .append(Component.text(currentStatusText, currentStatusColor))
                .append(Component.text("[", NamedTextColor.GRAY))
                .append(Component.text(currentStatus, effectiveColor))
                .append(Component.text("]", NamedTextColor.GRAY));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (p.isOnline()) {
                p.sendMessage(msg);
            }
        }, 20L);

    }
}
