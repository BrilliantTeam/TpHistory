package Rice.Chen.TpHistory;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class TeleportManager {
    private final Map<UUID, List<TeleportRecord>> playerHistory;
    private static final int MAX_HISTORY = 10;
    private final File dataFolder;
    private final TpHistory plugin;
    
    public TeleportManager(TpHistory plugin) {
        this.plugin = plugin;
        this.playerHistory = new HashMap<>();
        this.dataFolder = new File(plugin.getDataFolder(), "players");
        createDataFolder();
    }
    
    private void createDataFolder() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }
    
    private File getPlayerFile(UUID playerId) {
        return new File(dataFolder, playerId.toString() + ".yml");
    }
    
    private void loadPlayerData(UUID playerId) {
        if (playerHistory.containsKey(playerId)) {
            return; // 如果已經載入過，就不重複載入
        }
        
        File playerFile = getPlayerFile(playerId);
        if (!playerFile.exists()) {
            playerHistory.put(playerId, new ArrayList<>());
            return;
        }
        
        FileConfiguration data = YamlConfiguration.loadConfiguration(playerFile);
        List<TeleportRecord> history = new ArrayList<>();
        
        if (data.contains("history")) {
            for (String key : data.getConfigurationSection("history").getKeys(false)) {
                Location loc = data.getLocation("history." + key + ".location");
                long timestamp = data.getLong("history." + key + ".timestamp");
                
                if (loc != null) {
                    history.add(new TeleportRecord(loc, timestamp));
                }
            }
        }
        
        playerHistory.put(playerId, history);
    }
    
    private void savePlayerData(UUID playerId) {
        File playerFile = getPlayerFile(playerId);
        FileConfiguration data = YamlConfiguration.loadConfiguration(playerFile);
        List<TeleportRecord> history = playerHistory.get(playerId);
        
        // 清除舊資料
        data.set("history", null);
        
        // 儲存新資料
        for (int i = 0; i < history.size(); i++) {
            TeleportRecord record = history.get(i);
            String basePath = "history." + i;
            data.set(basePath + ".location", record.getLocation());
            data.set(basePath + ".timestamp", record.getTimestamp());
        }
        
        // 儲存到檔案
        try {
            data.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().warning("§c無法儲存玩家 " + playerId + " 的傳送歷史記錄：" + e.getMessage());
        }
    }
    
    public void saveAllData() {
        for (UUID playerId : playerHistory.keySet()) {
            savePlayerData(playerId);
        }
    }
    
    public void addTeleportRecord(Player player, Location location) {
        UUID playerId = player.getUniqueId();
        
        // 確保玩家資料已載入
        loadPlayerData(playerId);
        
        List<TeleportRecord> history = playerHistory.get(playerId);
        history.add(0, new TeleportRecord(location));
        
        while (history.size() > MAX_HISTORY) {
            history.remove(history.size() - 1);
        }
        
        // 即時儲存更改
        savePlayerData(playerId);
    }
    
    public List<TeleportRecord> getPlayerHistory(UUID playerId) {
        // 確保玩家資料已載入
        loadPlayerData(playerId);
        return playerHistory.getOrDefault(playerId, new ArrayList<>());
    }
    
    // 清理未使用的玩家資料（可選）
    public void clearUnusedData() {
        // 從記憶體中移除未使用的玩家資料
        playerHistory.clear();
    }
    
    // 刪除玩家資料（可選）
    public void deletePlayerData(UUID playerId) {
        File playerFile = getPlayerFile(playerId);
        if (playerFile.exists()) {
            playerFile.delete();
        }
        playerHistory.remove(playerId);
    }
}