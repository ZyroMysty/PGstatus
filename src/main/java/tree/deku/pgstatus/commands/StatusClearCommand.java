package tree.deku.pgstatus.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tree.deku.pgstatus.PGstatus;
import tree.deku.pgstatus.manager.StatusManager;

public class StatusClearCommand implements CommandExecutor {
    private final StatusManager statusManager;
    private final PGstatus plugin;

    public StatusClearCommand(StatusManager statusManager, PGstatus plugin) {
        this.statusManager = statusManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Not Available through console");
            return true;
        }

        statusManager.clearPlayerStatus(player.getUniqueId());
        statusManager.resetPlayerListName(player);

        player.sendMessage(plugin.messages().get("status-cleared"));


        return true;
    }
}
