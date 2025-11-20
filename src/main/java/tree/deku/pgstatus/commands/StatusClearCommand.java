package tree.deku.pgstatus.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import tree.deku.pgstatus.StatusManager;

public class StatusClearCommand implements CommandExecutor {
    private final StatusManager statusManager;

    public StatusClearCommand(StatusManager statusManager) {
        this.statusManager = statusManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Ingame, Bro.");
            return true;
        }

        if (!player.hasPermission("statustag.use")) {
            player.sendMessage("§cDafür hast du keine Rechte.");
            return true;
        }

        statusManager.clearPlayerStatus(player.getUniqueId());
        statusManager.resetPlayerListName(player);

        player.sendMessage("§aDein Status wurde entfernt.");
        return true;
    }
}
