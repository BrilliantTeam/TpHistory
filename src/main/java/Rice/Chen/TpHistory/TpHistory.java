package Rice.Chen.TpHistory;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TpHistory extends JavaPlugin implements Listener {
    private static TpHistory instance;
    private GuiListener guiListener;
    private TeleportManager teleportManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        this.teleportManager = new TeleportManager(this);  
        this.guiListener = new GuiListener(this, teleportManager);
        
        getServer().getPluginManager().registerEvents(guiListener, this);
        getServer().getPluginManager().registerEvents(this, this);
        
        getCommand("tpb").setExecutor(new TpHistoryCommand(teleportManager, guiListener));
        
        getLogger().info("TpHistory 插件已啟用！");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            guiListener.preloadBiomeData(event.getPlayer());
        });
    }
    
    @Override
    public void onDisable() {
        if (teleportManager != null) {
            teleportManager.saveAllData();
        }
        getLogger().info("TpHistory 插件已停用！");
    }
    
    public static TpHistory getInstance() {
        return instance;
    }
    
    public TeleportManager getTeleportManager() {
        return teleportManager;
    }
    
    public GuiListener getGuiListener() {
        return guiListener;
    }
}