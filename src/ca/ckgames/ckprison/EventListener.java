package ca.ckgames.ckprison;

import ca.ckgames.ckprison.mines.Mine;
import ca.ckgames.ckprison.mines.MineHandler;
import ca.ckgames.ckprison.ranks.Rank;
import ca.ckgames.ckprison.ranks.RankHandler;
import com.google.common.collect.Lists;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.Objects;

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
            databaseHandler.addNewPlayer(player, config.defaultRank);
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
    public void onInteraction(PlayerInteractEvent event) {
        if (!config.automaticProtection) return;
        // Handle placing block prevention.
        Player player = event.getPlayer();
        boolean canPlaceBlocks = Utils.isAdmin(player);

        if (event.isBlockInHand() && event.hasBlock() && !Objects.requireNonNull(event.getClickedBlock()).getType().isInteractable() && !canPlaceBlocks) {
            event.setCancelled(true);
            return;
        }

        ArrayList<Material> pickaxes = Lists.newArrayList(
                Material.DIAMOND_PICKAXE, Material.GOLDEN_PICKAXE,
                Material.IRON_PICKAXE, Material.NETHERITE_PICKAXE,
                Material.STONE_PICKAXE, Material.WOODEN_PICKAXE);

        if (event.getHand() == EquipmentSlot.HAND && event.hasItem() && pickaxes.contains(Objects.requireNonNull(event.getItem()).getType())) {
            if (config.pickaxeCommand == null || (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)) {
                return;
            }
            player.performCommand(config.pickaxeCommand);
        }
    }
}
