package ca.ckgames.ckprison;

import ca.ckgames.ckprison.mines.Mine;
import ca.ckgames.ckprison.mines.MineHandler;
import ca.ckgames.ckprison.ranks.Rank;
import ca.ckgames.ckprison.ranks.RankHandler;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class EventListener implements Listener {
    Config config;
    SQLiteHandler databaseHandler;
    MineHandler mineHandler;
    RankHandler rankHandler;

    public EventListener(Config config, SQLiteHandler databaseHandler, MineHandler mineHandler, RankHandler rankHandler) {
        this.config = config;
        this.databaseHandler = databaseHandler;
        this.mineHandler = mineHandler;
        this.rankHandler = rankHandler;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Handle player join events.
        Player player = event.getPlayer();
        if (databaseHandler.getPlayerRank(player) == null) {
            databaseHandler.addNewPlayer(player);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!config.automaticProtection) return;
        // Handle breaking block prevention.
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Mine mine = mineHandler.getMineFromBlock(block);
        boolean isAdmin = Utils.isAdmin(player);
        boolean isInMine = mine != null;
        boolean canBreakBlocks = isAdmin || isInMine;

        if (!isAdmin && isInMine && config.rankBasedAccess && mine.requiredRank != null) {
            Rank rank = rankHandler.getRank(databaseHandler.getPlayerRank(player));
            boolean isAllowedInMine = false;
            while (rank != null) {
                if (rank.equals(mine.requiredRank)) {
                    isAllowedInMine = true;
                    break;
                }
                rank = rank.inheritsFrom;
            }

            canBreakBlocks = isAllowedInMine;
        }

        if (!canBreakBlocks) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!config.automaticProtection) return;
        // Handle placing block prevention.
        Player player = event.getPlayer();
        boolean canPlaceBlocks = Utils.isAdmin(player);

        if (!canPlaceBlocks) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String rankName = databaseHandler.getPlayerRank(event.getPlayer());
        if (rankName == null) {
            return;
        }
        Rank rank = rankHandler.getRank(rankName);
        if (rank == null) {
            return;
        }
        event.setFormat(event.getFormat().replace("{prison_rank}", ChatColor.translateAlternateColorCodes('&', rank.tag)));
    }

    @EventHandler
    public void onPreprocessCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();
        Player player = event.getPlayer();

        // QOL for CKGAMES server. If/when publishing this plugin, should be moved to a private one.
        if (Utils.isAdmin(player) && command.contains("kill @e") && !command.contains("[")) {
            player.sendMessage(ChatColor.RED + "Hold it! Running this command without any limits (ie. kill @e[...]) is dangerous and kills every single entity. You should not use it!");
            event.setCancelled(true);
        }
    }
}
