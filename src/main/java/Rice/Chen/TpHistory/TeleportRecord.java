package Rice.Chen.TpHistory;

import org.bukkit.Location;

public class TeleportRecord {
    private final Location location;
    private final long timestamp;
    
    public TeleportRecord(Location location) {
        this.location = location;
        this.timestamp = System.currentTimeMillis();
    }
    
    // 用於從儲存中讀取資料的建構函數
    public TeleportRecord(Location location, long timestamp) {
        this.location = location;
        this.timestamp = timestamp;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
}