package tree.deku.pgstatus.manager;

import com.mojang.brigadier.Message;
import org.bukkit.configuration.file.FileConfiguration;
import tree.deku.pgstatus.PGstatus;

import java.io.File;

public class MessageManager {
    private final PGstatus plugin;
    private File file;
    private FileConfiguration config;

    public MessageManager(PGstatus plugin){
        this.plugin = plugin;
        load();
    }

    public void load(){

    }

}
