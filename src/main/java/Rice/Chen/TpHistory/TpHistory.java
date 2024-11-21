package Rice.Chen.TpHistory;

import org.bukkit.plugin.java.JavaPlugin;

public class TpHistory extends JavaPlugin {
    private static TpHistory instance;
    private GuiListener guiListener;
    private TeleportManager teleportManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // 初始化管理器和監聽器
        this.teleportManager = new TeleportManager(this);  // 傳入 plugin 實例
        this.guiListener = new GuiListener(teleportManager);
        
        // 註冊事件監聽器
        getServer().getPluginManager().registerEvents(guiListener, this);
        
        // 註冊指令
        getCommand("tpb").setExecutor(new TpHistoryCommand(teleportManager, guiListener));
        
        getLogger().info("TpHistory 插件已啟用！");
    }
    
    @Override
    public void onDisable() {
        // 儲存所有玩家的資料
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