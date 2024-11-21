package Rice.Chen.TpHistory;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuiListener implements Listener {
    private final TeleportManager teleportManager;
    private final SimpleDateFormat dateFormat;
    private final Pattern hexPattern;

    public GuiListener(TeleportManager teleportManager) {
        this.teleportManager = teleportManager;
        this.dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        this.hexPattern = Pattern.compile("#[a-fA-F0-9]{6}");
    }

    // 處理十六進制顏色代碼
    private String translateHexColorCodes(String message) {
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        
        while (matcher.find()) {
            String group = matcher.group();
            matcher.appendReplacement(buffer, ChatColor.of(group).toString());
        }
        
        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    private String translateWorldName(String worldName) {
        return switch (worldName.toLowerCase()) {
            case "world" -> "&f主世界";
            case "world_nether" -> "&f地獄";
            case "world_the_end" -> "&f終界";
            default -> worldName;
        };
    }

    private Material getBiomeMaterial(Biome biome) {
        try {
            return switch (biome) {
                // 海洋生態域
                case OCEAN -> Material.WATER_BUCKET;
                case DEEP_OCEAN -> Material.PRISMARINE_CRYSTALS;
                case FROZEN_OCEAN -> Material.ICE;
                case DEEP_FROZEN_OCEAN -> Material.PACKED_ICE;
                case COLD_OCEAN -> Material.COD;
                case DEEP_COLD_OCEAN -> Material.SALMON;
                case LUKEWARM_OCEAN -> Material.TROPICAL_FISH;
                case DEEP_LUKEWARM_OCEAN -> Material.PUFFERFISH;
                case WARM_OCEAN -> Material.TUBE_CORAL;
            
                // 海岸生態域
                case BEACH -> Material.SANDSTONE;
                case SNOWY_BEACH -> Material.SNOW;
                case STONY_SHORE -> Material.STONE;
            
                // 平地生態域
                case PLAINS -> Material.GRASS_BLOCK;
                case SUNFLOWER_PLAINS -> Material.SUNFLOWER;
                case MEADOW -> Material.GRASS;
                case SNOWY_PLAINS -> Material.SNOW_BLOCK;
            
                // 森林生態域
                case FOREST -> Material.OAK_SAPLING;
                case FLOWER_FOREST -> Material.ALLIUM;
                case BIRCH_FOREST -> Material.BIRCH_SAPLING;
                case OLD_GROWTH_BIRCH_FOREST -> Material.BIRCH_LOG;
                case DARK_FOREST -> Material.DARK_OAK_SAPLING;
                case WINDSWEPT_FOREST -> Material.SPRUCE_SAPLING;
            
                // 叢林生態域
                case JUNGLE -> Material.JUNGLE_SAPLING;
                case SPARSE_JUNGLE -> Material.VINE;
                case BAMBOO_JUNGLE -> Material.BAMBOO;
            
                // 針葉林生態域
                case SNOWY_TAIGA -> Material.SNOW;
                case TAIGA -> Material.SPRUCE_SAPLING;
                case OLD_GROWTH_PINE_TAIGA -> Material.SPRUCE_LOG;
                case OLD_GROWTH_SPRUCE_TAIGA -> Material.SPRUCE_LOG;
            
                // 乾燥生態域
                case DESERT -> Material.SAND;
                case BADLANDS -> Material.TERRACOTTA;
                case WOODED_BADLANDS -> Material.DARK_OAK_SAPLING;
                case ERODED_BADLANDS -> Material.RED_SANDSTONE;
            
                // 山地生態域
                case WINDSWEPT_HILLS -> Material.STONE;
                case WINDSWEPT_GRAVELLY_HILLS -> Material.GRAVEL;
                case JAGGED_PEAKS -> Material.STONE;
                case FROZEN_PEAKS -> Material.PACKED_ICE;
                case STONY_PEAKS -> Material.STONE;
            
                // 洞穴生態域
                case LUSH_CAVES -> Material.MOSS_BLOCK;
                case DRIPSTONE_CAVES -> Material.DRIPSTONE_BLOCK;
                case DEEP_DARK -> Material.SCULK;
            
                // 地獄生態域
                case NETHER_WASTES -> Material.NETHERRACK;
                case SOUL_SAND_VALLEY -> Material.SOUL_SAND;
                case CRIMSON_FOREST -> Material.CRIMSON_FUNGUS;
                case WARPED_FOREST -> Material.WARPED_FUNGUS;
                case BASALT_DELTAS -> Material.BASALT;
            
                // 終界生態域
                case THE_END -> Material.END_STONE;
                case END_HIGHLANDS -> Material.PURPUR_BLOCK;
                case END_MIDLANDS -> Material.CHORUS_FLOWER;
                case SMALL_END_ISLANDS -> Material.END_STONE_BRICKS;
                case END_BARRENS -> Material.PURPUR_PILLAR;
            
                // 河流生態域
                case RIVER -> Material.WATER_BUCKET;
                case FROZEN_RIVER -> Material.ICE;
            
                // 其他生態域
                case MUSHROOM_FIELDS -> Material.RED_MUSHROOM_BLOCK;
                case SWAMP -> Material.LILY_PAD;
                case MANGROVE_SWAMP -> Material.MANGROVE_ROOTS;
            
                // 預設材質
                default -> Material.ENDER_PEARL;
            };
        } catch (IllegalArgumentException e) {
            return Material.ENDER_PEARL;
        }
    }

    private String getBiomeDisplayName(Biome biome) {
        try {
            return switch (biome) {
                // 海洋生態域
                case OCEAN -> "&1海洋";
                case DEEP_OCEAN -> "&1深海";
                case FROZEN_OCEAN -> "&b寒凍海洋";
                case DEEP_FROZEN_OCEAN -> "&b冰凍深海";
                case COLD_OCEAN -> "&b寒冷海洋";
                case DEEP_COLD_OCEAN -> "&b寒冷深海";
                case LUKEWARM_OCEAN -> "&b溫和海洋";
                case DEEP_LUKEWARM_OCEAN -> "&b溫和深海";
                case WARM_OCEAN -> "&b溫暖海洋";
            
                // 海岸生態域
                case BEACH -> "&e沙灘";
                case SNOWY_BEACH -> "&f冰雪沙灘";
                case STONY_SHORE -> "&8石岸";
            
                // 平地生態域
                case PLAINS -> "&a平原";
                case SUNFLOWER_PLAINS -> "&e向日葵平原";
                case MEADOW -> "&a草甸";
                case SNOWY_PLAINS -> "&f雪原";
            
                // 森林生態域
                case FOREST -> "&2森林";
                case FLOWER_FOREST -> "&d繁花森林";
                case BIRCH_FOREST -> "&a樺木森林";
                case OLD_GROWTH_BIRCH_FOREST -> "&a原始樺木森林";
                case DARK_FOREST -> "&2黑森林";
                case WINDSWEPT_FOREST -> "&2風蝕森林";
            
                // 叢林生態域
                case JUNGLE -> "&2叢林";
                case SPARSE_JUNGLE -> "&2稀疏叢林";
                case BAMBOO_JUNGLE -> "&2竹林";
            
                // 季節性生態域
                case SNOWY_TAIGA -> "&f冰雪針葉林";
                case TAIGA -> "&2針葉林";
                case OLD_GROWTH_PINE_TAIGA -> "&2原始松木針葉林";
                case OLD_GROWTH_SPRUCE_TAIGA -> "&2原始杉木針葉林";
            
                // 乾燥生態域
                case DESERT -> "&e沙漠";
                case BADLANDS -> "&6惡地";
                case WOODED_BADLANDS -> "&6疏林惡地";
                case ERODED_BADLANDS -> "&6侵蝕惡地";
            
                // 山地生態域
                case WINDSWEPT_HILLS -> "&8風蝕丘陵";
                case WINDSWEPT_GRAVELLY_HILLS -> "&8風蝕礫質丘陵";
                case JAGGED_PEAKS -> "&f尖峭山峰";
                case FROZEN_PEAKS -> "&f霜凍山峰";
                case STONY_PEAKS -> "&8裸岩山峰";
            
                // 洞穴生態域
                case LUSH_CAVES -> "&a蒼鬱洞窟";
                case DRIPSTONE_CAVES -> "&8鐘乳石洞窟";
                case DEEP_DARK -> "&8深淵";
            
                // 地獄生態域
                case NETHER_WASTES -> "&c地獄荒原";
                case SOUL_SAND_VALLEY -> "&8靈魂砂谷";
                case CRIMSON_FOREST -> "&4緋紅森林";
                case WARPED_FOREST -> "&3詭異森林";
                case BASALT_DELTAS -> "&8玄武岩三角洲";
            
                // 終界生態域
                case THE_END -> "&5終界";
                case END_HIGHLANDS -> "&5終界高地";
                case END_MIDLANDS -> "&5終界內陸";
                case SMALL_END_ISLANDS -> "&5終界小島";
                case END_BARRENS -> "&5終界荒地";
            
                // 河流生態域
                case RIVER -> "&b河流";
                case FROZEN_RIVER -> "&b寒凍河流";
            
                // 其他生態域
                case MUSHROOM_FIELDS -> "&d蘑菇地";
                case SWAMP -> "&2沼澤";
                case MANGROVE_SWAMP -> "&2紅樹林沼澤";
            
                // 預設名稱
                default -> "&7未知生態域";
            };
        } catch (IllegalArgumentException e) {
            return "&7未知生態域";
        }
    }

    public void openTpHistory(Player player) {
        Inventory gui = Bukkit.createInventory(null, 36, translateHexColorCodes("&0&l近十次的傳送紀錄"));
        
        // 設置背景
        ItemStack background = new ItemStack(Material.STICK);
        ItemMeta meta = background.getItemMeta();
        meta.setCustomModelData(20036);
        meta.setDisplayName(" ");
        background.setItemMeta(meta);
        
        // 填充背景
        for (int i = 0; i < 36; i++) {
            gui.setItem(i, background);
        }
        
        // 添加返回主選單按鈕
        ItemStack menuButton = new ItemStack(Material.STICK);
        ItemMeta menuMeta = menuButton.getItemMeta();
        menuMeta.setCustomModelData(20004);
        menuMeta.setDisplayName(translateHexColorCodes("&f&l返回主界面"));
        menuButton.setItemMeta(menuMeta);
        gui.setItem(27, menuButton);
        
        List<TeleportRecord> history = teleportManager.getPlayerHistory(player.getUniqueId());
        
        // 如果沒有記錄，顯示提示訊息
        if (history.isEmpty()) {
            ItemStack noRecord = new ItemStack(Material.BARRIER);
            ItemMeta noRecordMeta = noRecord.getItemMeta();
            noRecordMeta.setDisplayName(translateHexColorCodes("#ff7a7a&l還沒有沒有傳送記錄呢！"));
            noRecord.setItemMeta(noRecordMeta);
            gui.setItem(13, noRecord);
            
            player.openInventory(gui);
            return;
        }
        
        int[] slots = {11, 12, 13, 14, 15, 20, 21, 22, 23, 24};
        
        for (int i = 0; i < slots.length && i < history.size(); i++) {
            TeleportRecord record = history.get(i);
            Location loc = record.getLocation();
            Biome biome = loc.getBlock().getBiome();
            
            ItemStack item = new ItemStack(getBiomeMaterial(biome), i + 1);
            ItemMeta itemMeta = item.getItemMeta();
            
            // 設置物品名稱
            itemMeta.setDisplayName(translateHexColorCodes(
                String.format("&9#%d &f| #cfffc0%s, %s, %s", 
                    i + 1,
                    loc.getBlockX(), 
                    loc.getBlockY(), 
                    loc.getBlockZ())
            ));
            
            // 設置說明文字
            List<String> lore = new ArrayList<>();
            lore.add(translateHexColorCodes("&7    "));
            lore.add(translateHexColorCodes(String.format("&7    世界：&f%s    ", translateWorldName(loc.getWorld().getName()))));
            lore.add(translateHexColorCodes(String.format("&7    生態：&f%s    ", getBiomeDisplayName(biome))));
            lore.add(translateHexColorCodes(String.format("&7    時間：&f%s    ", dateFormat.format(new Date(record.getTimestamp())))));
            lore.add(translateHexColorCodes("&7    "));
            lore.add(translateHexColorCodes("#e6bbf6點擊後傳回此處。"));
            itemMeta.setLore(lore);
            
            item.setItemMeta(itemMeta);
            gui.setItem(slots[i], item);
        }
        
        player.openInventory(gui);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        
        // 檢查是否為相同位置
        if (from.getWorld().equals(to.getWorld()) &&
            from.getBlockX() == to.getBlockX() &&
            from.getBlockY() == to.getBlockY() &&
            from.getBlockZ() == to.getBlockZ()) {
            return;
        }
        
        teleportManager.addTeleportRecord(player, to);
        
        // 發送可點擊的通知消息
        TextComponent message = new TextComponent(translateHexColorCodes("&7｜&6系統&7｜&f飯娘：&7已記錄了傳送位置！ &7[點此查看]"));
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpb"));
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder(translateHexColorCodes("&7點擊打開近十次的傳送紀錄")).create()));
        player.spigot().sendMessage(message);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(translateHexColorCodes("&0&l近十次的傳送紀錄"))) return;
        
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null) return;
        
        // 處理返回主選單按鈕
        if (event.getSlot() == 27 && clicked.getType() == Material.STICK) {
            player.closeInventory();
            player.performCommand("menu");
            return;
        }
        
        // 處理傳送按鈕點擊
        int slot = event.getSlot();
        int index = -1;
        
        if (slot >= 11 && slot <= 15) {
            index = slot - 11;
        } else if (slot >= 20 && slot <= 24) {
            index = slot - 20 + 5;
        }
        
        if (index >= 0) {
            List<TeleportRecord> history = teleportManager.getPlayerHistory(player.getUniqueId());
            if (index < history.size()) {
                Location loc = history.get(index).getLocation();
                player.teleport(loc);
                player.sendMessage(translateHexColorCodes("&7｜&6系統&7｜&f飯娘：#cfffc0已成功&7傳送到選擇的位置！"));
                player.closeInventory();
            }
        }
    }
}