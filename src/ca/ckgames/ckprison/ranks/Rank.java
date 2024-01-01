package ca.ckgames.ckprison.ranks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class Rank {
    public String name;
    public int rankupPrice;
    public String tag;
    public Rank inheritsFrom;
    public Rank nextRank;
    public String inheritsFromName;
    public String nextRankName;
    private final List<String> onRankupCommands;

    public Rank(String name, int rankupPrice, String tag, String inheritsFromName, String nextRankName, List<String> onRankupCommands) {
        this.name = name;
        this.rankupPrice = rankupPrice;
        this.tag = tag;
        this.inheritsFromName = inheritsFromName;
        this.nextRankName = nextRankName;
        this.onRankupCommands = onRankupCommands;
    }

    public void onRankAchieved(Player player) {
        for (String command : onRankupCommands) {
            // Run each command with console level access.
            // Not ideal, but for most cases this just simplifies user experience.
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                    .replaceAll("%p", player.getName())
                    .replaceAll("%d", player.getDisplayName())
                    .replaceAll("%r", name)
            );
        }
    }
}
