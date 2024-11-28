package Rice.Chen.TpHistory;

import org.bukkit.Material;
import org.bukkit.block.Block;
import java.util.HashSet;
import java.util.Set;

public class ElevatorStructure {
    private static final Set<Material> CARPET_PRESSURE_PLATES = new HashSet<>();
    
    static {
        // 添加所有地毯類型
        Material[] carpets = {
            Material.WHITE_CARPET, Material.ORANGE_CARPET, Material.MAGENTA_CARPET,
            Material.LIGHT_BLUE_CARPET, Material.YELLOW_CARPET, Material.LIME_CARPET,
            Material.PINK_CARPET, Material.GRAY_CARPET, Material.LIGHT_GRAY_CARPET,
            Material.CYAN_CARPET, Material.PURPLE_CARPET, Material.BLUE_CARPET,
            Material.BROWN_CARPET, Material.GREEN_CARPET, Material.RED_CARPET,
            Material.BLACK_CARPET, Material.MOSS_CARPET
        };
        
        // 添加所有壓力板類型
        Material[] pressurePlates = {
            Material.OAK_PRESSURE_PLATE, Material.SPRUCE_PRESSURE_PLATE,
            Material.BIRCH_PRESSURE_PLATE, Material.JUNGLE_PRESSURE_PLATE,
            Material.ACACIA_PRESSURE_PLATE, Material.DARK_OAK_PRESSURE_PLATE,
            Material.MANGROVE_PRESSURE_PLATE, Material.CHERRY_PRESSURE_PLATE,
            Material.BAMBOO_PRESSURE_PLATE, Material.CRIMSON_PRESSURE_PLATE,
            Material.WARPED_PRESSURE_PLATE, Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
            Material.HEAVY_WEIGHTED_PRESSURE_PLATE, Material.STONE_PRESSURE_PLATE,
            Material.POLISHED_BLACKSTONE_PRESSURE_PLATE
        };
        
        for (Material carpet : carpets) {
            CARPET_PRESSURE_PLATES.add(carpet);
        }
        for (Material plate : pressurePlates) {
            CARPET_PRESSURE_PLATES.add(plate);
        }
    }

    public static boolean isValidStructure(Block block) {
        if (block.getType() != Material.QUARTZ_BLOCK) {
            return false;
        }

        Block belowBlock = block.getRelative(0, -1, 0);
        return belowBlock.getType() == Material.REDSTONE_BLOCK ||
                CARPET_PRESSURE_PLATES.contains(block.getRelative(0, 1, 0).getType());
    }

    public static boolean isElevatorPosition(Block playerBlock) {
        Block belowBlock = playerBlock.getRelative(0, -1, 0);
        
        // 對於地毯/壓力板電梯
        if (CARPET_PRESSURE_PLATES.contains(playerBlock.getType())) {
            return belowBlock.getType() == Material.QUARTZ_BLOCK;
        }
        
        // 對於紅石方塊電梯
        if (belowBlock.getType() == Material.QUARTZ_BLOCK) {
            return belowBlock.getRelative(0, -1, 0).getType() == Material.REDSTONE_BLOCK;
        }
        
        return false;
    }
}