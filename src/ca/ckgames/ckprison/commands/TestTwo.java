package ca.ckgames.ckprison.commands;

import ca.ckgames.ckprison.mines.Mine;
import ca.ckgames.ckprison.mines.MineHandler;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class TestTwo implements CommandExecutor {
    private final Plugin plugin;
    private final MineHandler mineHandler;

    public TestTwo(Plugin plugin, MineHandler mineHandler) {
        this.plugin = plugin;
        this.mineHandler = mineHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            String mineName = "mine_a";
            if (args.length >= 1) mineName = args[0];

            Mine mine = null;
            for (Mine m : mineHandler.mineList) {
                if (m.name.equalsIgnoreCase(mineName)) {
                    mine = m;
                    break;
                }
            }

            if (mine == null) {
                player.sendMessage("This mine does not exist.");
                return false;
            }

            Location spawnPosition = player.getLocation();
            World world = player.getWorld();

            List<BlockDisplay> entities = new ArrayList<>();
            for (int offsetX = 0; offsetX <= mine.bounds.getWidthX(); offsetX += 1) {
                for (int offsetY = 0; offsetY <= mine.bounds.getHeight(); offsetY += 1) {
                    for (int offsetZ = 0; offsetZ <= mine.bounds.getWidthZ(); offsetZ += 1) {
                        if (offsetX != 0 && offsetY != 0 && offsetZ != 0) continue;
                        BlockDisplay blockDisplay = (BlockDisplay) world.spawnEntity(spawnPosition.clone().add(offsetX, offsetY, offsetZ), EntityType.BLOCK_DISPLAY);
                        blockDisplay.setBlock(mine.getRandomMaterial().createBlockData());
                        entities.add(blockDisplay);
                    }
                }
            }

            new BukkitRunnable() {
                int executions = 0;
                @Override
                public void run() {
                    for (BlockDisplay display : entities) {
                        display.setRotation(executions, 0);
                    }

                    executions += 1;
                    if (executions > 360) {
                        for (BlockDisplay display : entities) {
                            display.remove();
                            this.cancel();
                        }
                    }
                }
            }.runTaskTimer(plugin, 1, 1);
            return true;
        } else {
            sender.sendMessage("You need a position to run this command.");
            return false;
        }
    }
}
