package tree.deku.pgstatus.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import tree.deku.pgstatus.manager.StatusManager;

public class StatusListener implements Listener {
    private final StatusManager statusManager;

    public StatusListener(StatusManager statusManager) {
        this.statusManager = statusManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        statusManager.applyStatus(p);
    }
}
