package Rice.Chen.TpHistory;

import com.google.common.collect.ImmutableMap;
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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

public class GuiListener implements Listener {
    private final TeleportManager teleportManager;
    private final SimpleDateFormat dateFormat;
    private final Pattern hexPattern;

    public record CachedTeleportData(TeleportRecord record, Biome biome) {}

    private final ConcurrentHashMap<UUID, List<CachedTeleportData>> playerBiomeCache = new ConcurrentHashMap<>();

    public CompletableFuture<Void> preloadBiomeData(Player player) {
        List<TeleportRecord> history = teleportManager.getPlayerHistory(player.getUniqueId());
        List<CompletableFuture<CachedTeleportData>> futures = new ArrayList<>();
        
        for (TeleportRecord record : history) {
            Location loc = record.getLocation();
            if (loc != null && loc.getWorld() != null) {
                CompletableFuture<CachedTeleportData> future = new CompletableFuture<>();
                
                Bukkit.getServer().getRegionScheduler().execute(plugin, loc, () -> {
                    try {
                        Biome biome = loc.getBlock().getBiome();
                        future.complete(new CachedTeleportData(record, biome));
                    } catch (Exception e) {
                        future.complete(new CachedTeleportData(record, Biome.PLAINS));
                    }
                });
                
                futures.add(future);
            }
        }
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenAccept(v -> {
                    List<CachedTeleportData> cachedData = new ArrayList<>();
                    for (CompletableFuture<CachedTeleportData> future : futures) {
                        try {
                            cachedData.add(future.get());
                        } catch (Exception e) {
                            plugin.getLogger().warning("獲取生態域數據失敗：" + e.getMessage());
                        }
                    }
                    playerBiomeCache.put(player.getUniqueId(), cachedData);
                });
    }

    public record BiomeInfo(Material material, String displayName) {}
    
    private static final ImmutableMap<Biome, BiomeInfo> BIOME_INFO = ImmutableMap.<Biome, BiomeInfo>builder()
        // 海洋生態域
        .put(Biome.OCEAN, new BiomeInfo(Material.WATER_BUCKET, "&1海洋"))
        .put(Biome.DEEP_OCEAN, new BiomeInfo(Material.PRISMARINE_CRYSTALS, "&1深海"))
        .put(Biome.FROZEN_OCEAN, new BiomeInfo(Material.ICE, "&b寒凍海洋"))
        .put(Biome.DEEP_FROZEN_OCEAN, new BiomeInfo(Material.PACKED_ICE, "&b寒凍深海"))
        .put(Biome.COLD_OCEAN, new BiomeInfo(Material.COD, "&b寒冷海洋"))
        .put(Biome.DEEP_COLD_OCEAN, new BiomeInfo(Material.SALMON, "&b寒冷深海"))
        .put(Biome.LUKEWARM_OCEAN, new BiomeInfo(Material.TROPICAL_FISH, "&b溫和海洋"))
        .put(Biome.DEEP_LUKEWARM_OCEAN, new BiomeInfo(Material.PUFFERFISH, "&b溫和深海"))
        .put(Biome.WARM_OCEAN, new BiomeInfo(Material.TUBE_CORAL, "&b溫暖海洋"))

        // 海岸生態域
        .put(Biome.BEACH, new BiomeInfo(Material.SANDSTONE, "&e沙灘"))
        .put(Biome.SNOWY_BEACH, new BiomeInfo(Material.SNOW, "&f冰雪沙灘"))
        .put(Biome.STONY_SHORE, new BiomeInfo(Material.STONE, "&8石岸"))

        // 平地生態域
        .put(Biome.PLAINS, new BiomeInfo(Material.GRASS_BLOCK, "&a平原"))
        .put(Biome.SUNFLOWER_PLAINS, new BiomeInfo(Material.SUNFLOWER, "&e向日葵平原"))
        .put(Biome.MEADOW, new BiomeInfo(Material.GRASS, "&a草甸"))
        .put(Biome.SNOWY_PLAINS, new BiomeInfo(Material.SNOW_BLOCK, "&f雪原"))
        .put(Biome.SAVANNA, new BiomeInfo(Material.ACACIA_SAPLING, "&6莽原"))
        .put(Biome.SAVANNA_PLATEAU, new BiomeInfo(Material.ACACIA_LOG, "&6莽原高地"))
        .put(Biome.WINDSWEPT_SAVANNA, new BiomeInfo(Material.ACACIA_LEAVES, "&6風蝕莽原"))

        // 森林生態域
        .put(Biome.FOREST, new BiomeInfo(Material.OAK_SAPLING, "&2森林"))
        .put(Biome.FLOWER_FOREST, new BiomeInfo(Material.ALLIUM, "&d繁花森林"))
        .put(Biome.BIRCH_FOREST, new BiomeInfo(Material.BIRCH_SAPLING, "&a樺木森林"))
        .put(Biome.OLD_GROWTH_BIRCH_FOREST, new BiomeInfo(Material.BIRCH_LOG, "&a原生樺木森林"))
        .put(Biome.DARK_FOREST, new BiomeInfo(Material.DARK_OAK_SAPLING, "&2黑森林"))
        .put(Biome.WINDSWEPT_FOREST, new BiomeInfo(Material.SPRUCE_SAPLING, "&2風蝕森林"))
        .put(Biome.GROVE, new BiomeInfo(Material.POWDER_SNOW, "&f雪林"))
        .put(Biome.CHERRY_GROVE, new BiomeInfo(Material.CHERRY_SAPLING, "&d櫻花樹林"))

        // 叢林生態域
        .put(Biome.JUNGLE, new BiomeInfo(Material.JUNGLE_SAPLING, "&2叢林"))
        .put(Biome.SPARSE_JUNGLE, new BiomeInfo(Material.VINE, "&2稀疏叢林"))
        .put(Biome.BAMBOO_JUNGLE, new BiomeInfo(Material.BAMBOO, "&2竹林"))

        // 針葉林生態域
        .put(Biome.SNOWY_TAIGA, new BiomeInfo(Material.SNOW, "&f冰雪針葉林"))
        .put(Biome.TAIGA, new BiomeInfo(Material.SPRUCE_SAPLING, "&2針葉林"))
        .put(Biome.OLD_GROWTH_PINE_TAIGA, new BiomeInfo(Material.SPRUCE_LOG, "&2原生松木針葉林"))
        .put(Biome.OLD_GROWTH_SPRUCE_TAIGA, new BiomeInfo(Material.SPRUCE_LOG, "&2原生杉木針葉林"))

        // 特殊寒冷生態域
        .put(Biome.ICE_SPIKES, new BiomeInfo(Material.PACKED_ICE, "&b冰刺"))
        .put(Biome.SNOWY_SLOPES, new BiomeInfo(Material.SNOW, "&f雪坡"))

        // 乾燥生態域
        .put(Biome.DESERT, new BiomeInfo(Material.SAND, "&e沙漠"))
        .put(Biome.BADLANDS, new BiomeInfo(Material.TERRACOTTA, "&6惡地"))
        .put(Biome.WOODED_BADLANDS, new BiomeInfo(Material.DARK_OAK_SAPLING, "&6疏林惡地"))
        .put(Biome.ERODED_BADLANDS, new BiomeInfo(Material.RED_SANDSTONE, "&6侵蝕惡地"))

        // 山地生態域
        .put(Biome.WINDSWEPT_HILLS, new BiomeInfo(Material.STONE, "&8風蝕丘陵"))
        .put(Biome.WINDSWEPT_GRAVELLY_HILLS, new BiomeInfo(Material.GRAVEL, "&8風蝕礫質丘陵"))
        .put(Biome.JAGGED_PEAKS, new BiomeInfo(Material.STONE, "&f尖峭山峰"))
        .put(Biome.FROZEN_PEAKS, new BiomeInfo(Material.PACKED_ICE, "&f霜凍山峰"))
        .put(Biome.STONY_PEAKS, new BiomeInfo(Material.STONE, "&8裸岩山峰"))

        // 洞穴生態域
        .put(Biome.LUSH_CAVES, new BiomeInfo(Material.MOSS_BLOCK, "&a蒼鬱洞窟"))
        .put(Biome.DRIPSTONE_CAVES, new BiomeInfo(Material.DRIPSTONE_BLOCK, "&8鐘乳石洞窟"))
        .put(Biome.DEEP_DARK, new BiomeInfo(Material.SCULK, "&8深淵"))

        // 地獄生態域
        .put(Biome.NETHER_WASTES, new BiomeInfo(Material.NETHERRACK, "&c地獄荒原"))
        .put(Biome.SOUL_SAND_VALLEY, new BiomeInfo(Material.SOUL_SAND, "&8靈魂砂谷"))
        .put(Biome.CRIMSON_FOREST, new BiomeInfo(Material.CRIMSON_FUNGUS, "&4緋紅森林"))
        .put(Biome.WARPED_FOREST, new BiomeInfo(Material.WARPED_FUNGUS, "&3扭曲森林"))
        .put(Biome.BASALT_DELTAS, new BiomeInfo(Material.BASALT, "&8玄武岩三角洲"))

        // 終界生態域
        .put(Biome.THE_END, new BiomeInfo(Material.END_STONE, "&5終界"))
        .put(Biome.END_HIGHLANDS, new BiomeInfo(Material.PURPUR_BLOCK, "&5終界高地"))
        .put(Biome.END_MIDLANDS, new BiomeInfo(Material.CHORUS_FLOWER, "&5終界平地"))
        .put(Biome.SMALL_END_ISLANDS, new BiomeInfo(Material.END_STONE_BRICKS, "&5終界小島"))
        .put(Biome.END_BARRENS, new BiomeInfo(Material.PURPUR_PILLAR, "&5終界荒地"))
        .put(Biome.THE_VOID, new BiomeInfo(Material.BEDROCK, "&8虛空"))

        // 河流生態域
        .put(Biome.RIVER, new BiomeInfo(Material.WATER_BUCKET, "&b河流"))
        .put(Biome.FROZEN_RIVER, new BiomeInfo(Material.ICE, "&b寒凍河流"))

        // 其他生態域
        .put(Biome.MUSHROOM_FIELDS, new BiomeInfo(Material.RED_MUSHROOM_BLOCK, "&d蘑菇地"))
        .put(Biome.SWAMP, new BiomeInfo(Material.LILY_PAD, "&2沼澤"))
        .put(Biome.MANGROVE_SWAMP, new BiomeInfo(Material.MANGROVE_ROOTS, "&2紅樹林沼澤"))
        .build();

    private static final BiomeInfo DEFAULT_BIOME_INFO = new BiomeInfo(Material.ENDER_PEARL, "&7未知生態域");

    private final TpHistory plugin;

    public GuiListener(TpHistory plugin, TeleportManager teleportManager) {
        this.plugin = plugin;
        this.teleportManager = teleportManager;
        this.dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        this.hexPattern = Pattern.compile("#[a-fA-F0-9]{6}");
    }

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
        return BIOME_INFO.getOrDefault(biome, DEFAULT_BIOME_INFO).material();
    }

    private String getBiomeDisplayName(Biome biome) {
        return BIOME_INFO.getOrDefault(biome, DEFAULT_BIOME_INFO).displayName();
    }

    public void openTpHistory(Player player) {
        Inventory gui = Bukkit.createInventory(null, 36, translateHexColorCodes("&0&l近十次的傳送紀錄"));
        
        ItemStack background = new ItemStack(Material.STICK);
        ItemMeta meta = background.getItemMeta();
        meta.setCustomModelData(20036);
        meta.setDisplayName(" ");
        background.setItemMeta(meta);
        
        for (int i = 0; i < 36; i++) {
            gui.setItem(i, background);
        }

        ItemStack menuButton = new ItemStack(Material.STICK);
        ItemMeta menuMeta = menuButton.getItemMeta();
        menuMeta.setCustomModelData(20004);
        menuMeta.setDisplayName(translateHexColorCodes("&f&l返回主界面"));
        menuButton.setItemMeta(menuMeta);
        gui.setItem(27, menuButton);
        
        List<TeleportRecord> history = teleportManager.getPlayerHistory(player.getUniqueId());
        
        if (history.isEmpty()) {
            ItemStack noRecord = new ItemStack(Material.BARRIER);
            ItemMeta noRecordMeta = noRecord.getItemMeta();
            noRecordMeta.setDisplayName(translateHexColorCodes("#ff7a7a&l還沒有沒有傳送記錄呢！"));
            noRecord.setItemMeta(noRecordMeta);
            gui.setItem(13, noRecord);
            
            player.openInventory(gui);
            return;
        }
        
        List<CachedTeleportData> cachedData = playerBiomeCache.getOrDefault(player.getUniqueId(), new ArrayList<>());
        
        if (cachedData.isEmpty() || cachedData.size() < history.size()) {
            ItemStack loading = new ItemStack(Material.CLOCK);
            ItemMeta loadingMeta = loading.getItemMeta();
            loadingMeta.setDisplayName(translateHexColorCodes("#ffbc61&l資料加載中..."));
            List<String> lore = new ArrayList<>();
            lore.add(translateHexColorCodes("&7請稍後再試。"));
            loadingMeta.setLore(lore);
            loading.setItemMeta(loadingMeta);
            gui.setItem(13, loading);
            
            player.openInventory(gui);
            
            preloadBiomeData(player).thenRun(() -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (player.getOpenInventory().getTitle().equals(translateHexColorCodes("&0&l近十次的傳送紀錄"))) {
                        updateTpHistoryGUI(player, gui);
                    }
                });
            });
            return;
        }
        
        updateTpHistoryGUI(player, gui);
    }

    private void updateTpHistoryGUI(Player player, Inventory gui) {
        List<CachedTeleportData> cachedData = playerBiomeCache.getOrDefault(player.getUniqueId(), new ArrayList<>());
        int[] slots = {11, 12, 13, 14, 15, 20, 21, 22, 23, 24};
        
        for (int i = 0; i < slots.length && i < cachedData.size(); i++) {
            CachedTeleportData data = cachedData.get(i);
            TeleportRecord record = data.record();
            Biome biome = data.biome();
            Location loc = record.getLocation();
            
            ItemStack item = new ItemStack(getBiomeMaterial(biome), i + 1);
            ItemMeta itemMeta = item.getItemMeta();
            
            itemMeta.setDisplayName(translateHexColorCodes(
                String.format("&9#%d &f| #cfffc0%s, %s, %s", 
                    i + 1,
                    loc.getBlockX(), 
                    loc.getBlockY(), 
                    loc.getBlockZ())
            ));
            
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
        
        if (player.getOpenInventory().getTitle().equals(translateHexColorCodes("&0&l近十次的傳送紀錄"))) {
            player.updateInventory();
        } else {
            player.openInventory(gui);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        
        if (teleportManager.isElevatorTeleport(from, to) || 
            event.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) {
            return;
        }
        
        if (from.getWorld().equals(to.getWorld()) &&
            from.getBlockX() == to.getBlockX() &&
            from.getBlockY() == to.getBlockY() &&
            from.getBlockZ() == to.getBlockZ()) {
            return;
        }
        
        List<TeleportRecord> history = teleportManager.getPlayerHistory(player.getUniqueId());
        boolean isDuplicate = history.stream().anyMatch(record -> {
            Location loc = record.getLocation();
            return loc.getWorld().equals(from.getWorld()) &&
                loc.getBlockX() == from.getBlockX() &&
                loc.getBlockY() == from.getBlockY() &&
                loc.getBlockZ() == from.getBlockZ();
        });

        if (isDuplicate) {
            /*
            TextComponent message1 = new TextComponent(translateHexColorCodes("&7｜&6系統&7｜&f飯娘：&7您進行了傳送，但是此位置已在記錄中，"));
            TextComponent message2 = new TextComponent(translateHexColorCodes("&7｜&6系統&7｜&f飯娘：&7因此不會重複紀錄，點此查看#e6bbf6近期傳送紀錄&7。"));
            
            message1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpb"));
            message1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new ComponentBuilder(translateHexColorCodes("&7點擊後開啟近十次的傳送紀錄")).create()));
            message2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpb"));
            message2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new ComponentBuilder(translateHexColorCodes("&7點擊後開啟近十次的傳送紀錄")).create()));
                
            player.spigot().sendMessage(message1);
            player.spigot().sendMessage(message2);
            */
            return;
        }
        
        teleportManager.addTeleportRecord(player, from);
    
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            preloadBiomeData(player);
        });
        
        /*
        TextComponent message = new TextComponent(translateHexColorCodes("&7｜&6系統&7｜&f飯娘：&7已記錄傳送前的位置，點此查看#e6bbf6近期傳送紀錄&7。"));
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpb"));
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder(translateHexColorCodes("&7點擊後開啟近十次的傳送紀錄")).create()));
        player.spigot().sendMessage(message);
        */
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(translateHexColorCodes("&0&l近十次的傳送紀錄"))) return;
        
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null) return;
        
        if (event.getSlot() == 27 && clicked.getType() == Material.STICK) {
            player.closeInventory();
            player.performCommand("menu");
            return;
        }
        
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
                player.teleportAsync(loc);
                player.sendMessage(translateHexColorCodes("&7｜&6系統&7｜&f飯娘：#cfffc0已成功&7傳送到選擇的位置！"));
                player.closeInventory();
            }
        }
    }
}