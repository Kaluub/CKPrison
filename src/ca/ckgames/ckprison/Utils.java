package ca.ckgames.ckprison;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Utils {
    public static boolean isAdmin(Player player) {
        return player.isOp() || player.hasPermission("ckprison.admin");
    }

    public static String format(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String format(String text, Object... args) {
        return ChatColor.translateAlternateColorCodes('&', String.format(text, args));
    }
}
