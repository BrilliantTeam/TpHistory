package Rice.Chen.TpHistory;

import org.bukkit.Location;
import org.bukkit.block.Block;
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
            return;
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
        
        data.set("history", null);
        
        for (int i = 0; i < history.size(); i++) {
            TeleportRecord record = history.get(i);
            String basePath = "history." + i;
            data.set(basePath + ".location", record.getLocation());
            data.set(basePath + ".timestamp", record.getTimestamp());
        }
        
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
    
    public boolean isElevatorTeleport(Location from, Location to) {
        // 檢查是否在同一世界
        if (!from.getWorld().equals(to.getWorld())) {
            return false;
        }
        
        // 檢查X和Z是否相同
        if (from.getBlockX() != to.getBlockX() || 
            from.getBlockZ() != to.getBlockZ()) {
            return false;
        }
        
        // 檢查Y軸差距是否大於2格
        int minY = Math.min(from.getBlockY(), to.getBlockY());
        int maxY = Math.max(from.getBlockY(), to.getBlockY());
        if (maxY - minY < 2) {
            return false;
        }

        // 檢查玩家位置是否為電梯位置
        Block fromBlock = from.getBlock();
        Block toBlock = to.getBlock();
        
        return ElevatorStructure.isElevatorPosition(fromBlock) && 
                ElevatorStructure.isElevatorPosition(toBlock);
    }
    
    public void addTeleportRecord(Player player, Location location) {
        UUID playerId = player.getUniqueId();
        loadPlayerData(playerId);
        List<TeleportRecord> history = playerHistory.get(playerId);
        
        boolean isDuplicate = history.stream().anyMatch(record -> {
            Location loc = record.getLocation();
            return loc.getWorld().equals(location.getWorld()) &&
                loc.getBlockX() == location.getBlockX() &&
                loc.getBlockY() == location.getBlockY() &&
                loc.getBlockZ() == location.getBlockZ();
        });
        
        if (!isDuplicate) {
            history.add(0, new TeleportRecord(location));
            
            while (history.size() > MAX_HISTORY) {
                history.remove(history.size() - 1);
            }
            
            savePlayerData(playerId);
        }
    }
    
    public List<TeleportRecord> getPlayerHistory(UUID playerId) {
        loadPlayerData(playerId);
        return playerHistory.getOrDefault(playerId, new ArrayList<>());
    }
    
    public void clearUnusedData() {
        playerHistory.clear();
    }
    
    public void deletePlayerData(UUID playerId) {
        File playerFile = getPlayerFile(playerId);
        if (playerFile.exists()) {
            playerFile.delete();
        }
        playerHistory.remove(playerId);
    }
}