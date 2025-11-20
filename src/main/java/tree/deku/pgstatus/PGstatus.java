package tree.deku.pgstatus;

import org.bukkit.plugin.java.JavaPlugin;
import tree.deku.pgstatus.commands.StatusClearCommand;
import tree.deku.pgstatus.commands.StatusCommand;
import tree.deku.pgstatus.listeners.StatusListener;

public final class PGstatus extends JavaPlugin {

    private static  PGstatus instance;
    private StatusManager statusManager;
    private BlacklistManager blacklistManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        statusManager = new StatusManager(this);
        blacklistManager = new BlacklistManager(this);

        StatusCommand statusCmd = new StatusCommand(this, statusManager, blacklistManager);
        getCommand("status").setExecutor(statusCmd);
        getCommand("status").setTabCompleter(statusCmd);

        getCommand("statusclear").setExecutor(new StatusClearCommand(statusManager));

        getServer().getPluginManager().registerEvents(new StatusListener(statusManager), this);

    }

    public static PGstatus getInstance() {
        return instance;
    }

    public StatusManager getStatusManager() {
        return statusManager;
    }

    public BlacklistManager getBlacklistManager(){
        return blacklistManager;
    }
}
