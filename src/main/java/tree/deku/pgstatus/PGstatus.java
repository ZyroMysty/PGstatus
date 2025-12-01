package tree.deku.pgstatus;

import org.bukkit.plugin.java.JavaPlugin;
import tree.deku.pgstatus.commands.*;
import tree.deku.pgstatus.listeners.StatusListener;
import tree.deku.pgstatus.manager.BlacklistManager;
import tree.deku.pgstatus.manager.MessageManager;
import tree.deku.pgstatus.manager.StatusManager;

public final class PGstatus extends JavaPlugin {

    private static PGstatus instance;
    private StatusManager statusManager;
    private BlacklistManager blacklistManager;
    private MessageManager messageManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        statusManager = new StatusManager(this);
        blacklistManager = new BlacklistManager(this);
        messageManager = new MessageManager(this);

        StatusCommand statusCmd = new StatusCommand(this, statusManager, blacklistManager);
        getCommand("status").setExecutor(statusCmd);
        getCommand("status").setTabCompleter(statusCmd);

        RemovesStrikesCommand removesStrikesCommand = new RemovesStrikesCommand(blacklistManager, this);
        getCommand("removestrikes").setExecutor(removesStrikesCommand);
        getCommand("removestrikes").setTabCompleter(removesStrikesCommand);

        CheckStrikesCommand checkCmd = new CheckStrikesCommand(blacklistManager, this);
        getCommand("checkstrikes").setExecutor(checkCmd);
        getCommand("checkstrikes").setTabCompleter(checkCmd);

        BlacklistCommand blacklistCmd = new BlacklistCommand(blacklistManager, this);
        getCommand("blacklist").setExecutor(blacklistCmd);
        getCommand("blacklist").setTabCompleter(blacklistCmd);

        getCommand("statusclear").setExecutor(new StatusClearCommand(statusManager,this));

        getServer().getPluginManager().registerEvents(new StatusListener(this,statusManager), this);

    }

    public static PGstatus getInstance() {
        return instance;
    }

    public StatusManager getStatusManager() {
        return statusManager;
    }

    public BlacklistManager getBlacklistManager() {
        return blacklistManager;
    }

    public MessageManager messages() {
        return messageManager;
    }
}
