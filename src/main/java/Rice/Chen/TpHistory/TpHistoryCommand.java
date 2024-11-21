package Rice.Chen.TpHistory;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpHistoryCommand implements CommandExecutor {
    private final TeleportManager teleportManager;
    private final GuiListener guiListener;
    
    public TpHistoryCommand(TeleportManager teleportManager, GuiListener guiListener) {
        this.teleportManager = teleportManager;
        this.guiListener = guiListener;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§7｜§6系統§7｜§f飯娘：§c這個指令只能由玩家使用！");
            return true;
        }
        
        guiListener.openTpHistory((Player) sender);
        return true;
    }
}