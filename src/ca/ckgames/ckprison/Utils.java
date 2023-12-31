package ca.ckgames.ckprison;

import org.bukkit.entity.Player;

public class Utils {
    public static boolean isAdmin(Player player) {
        return player.isOp() || player.hasPermission("ckprison.admin");
    }
}
