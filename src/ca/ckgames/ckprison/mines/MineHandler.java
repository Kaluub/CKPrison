package ca.ckgames.ckprison.mines;

import ca.ckgames.ckprison.Config;
import ca.ckgames.ckprison.ranks.Rank;
import ca.ckgames.ckprison.ranks.RankHandler;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class MineHandler {
    public final List<Mine> mineList = new ArrayList<>();
    private final RankHandler rankHandler;
    private final Plugin plugin;

    public MineHandler(RankHandler rankHandler, Plugin plugin) {
        this.rankHandler = rankHandler;
        this.plugin = plugin;
    }

    public List<MineLoadingResult> loadMines(Config config, FileConfiguration configFile) {
        clearMines();

        List<MineLoadingResult> results = new ArrayList<>();
        ConfigurationSection minesSection = configFile.getConfigurationSection("mines");

        if (minesSection == null) {
            results.add(new MineLoadingResult(false, "Mines field is missing or empty in the configuration file."));
            return results;
        }

        for (String mineName : minesSection.getKeys(false)) {
            ConfigurationSection mineConfig = minesSection.getConfigurationSection(mineName);
            if (mineConfig == null) {
                results.add(new MineLoadingResult(false, String.format(
                        "Mine %s is not valid.",
                        mineName
                )));
                continue;
            }

            String worldName = mineConfig.getString("world");
            if (worldName == null) {
                results.add(new MineLoadingResult(false, String.format(
                        "Mine %s needs to define a world.",
                        mineName
                )));
                continue;
            }

            World world = plugin.getServer().getWorld(worldName);
            if (world == null) {
                results.add(new MineLoadingResult(false, String.format(
                        "Mine %s defines an invalid world name: %s",
                        mineName,
                        worldName
                )));
                continue;
            }

            List<Integer> startList = mineConfig.getIntegerList("start");
            List<Integer> endList = mineConfig.getIntegerList("end");
            if (startList.size() != 3 || endList.size() != 3) {
                results.add(new MineLoadingResult(false, String.format(
                        "Mine %s has an invalid start or end position.",
                        mineName
                )));
                continue;
            }

            Vector start = new Vector(startList.get(0), startList.get(1), startList.get(2));
            Vector end = new Vector(endList.get(0), endList.get(1), endList.get(2));
            BoundingBox bounds = BoundingBox.of(start, end);

            if (
                    bounds.getHeight() > config.maximumMineSize
                    || bounds.getWidthX() > config.maximumMineSize
                    || bounds.getWidthZ() > config.maximumMineSize
            ) {
                results.add(new MineLoadingResult(false, String.format(
                        "Mine %s is larger than your maximum mine size in the config.",
                        mineName
                )));
                continue;
            }

            List<String> materialNames = mineConfig.getStringList("blocks");
            List<Double> weights = mineConfig.getDoubleList("weights");

            if (materialNames.isEmpty() || weights.isEmpty() || materialNames.size() != weights.size()) {
                results.add(new MineLoadingResult(false, String.format(
                        "Mine %s is missing block or weight entries (are the list sizes not equal?).",
                        mineName
                )));
                continue;
            }

            List<Material> blocks = new ArrayList<>();

            boolean error = false;
            for (String materialName : materialNames) {
                Material block = Material.matchMaterial(materialName);
                if (block == null || !block.isBlock()) {
                    results.add(new MineLoadingResult(false, String.format(
                            "Mine %s contains invalid element in blocks list: %s",
                            mineName,
                            materialName
                    )));
                    error = true;
                }
                blocks.add(block);
            }
            if (error) continue;

            String requiredRankName = mineConfig.getString("rank", null);
            Rank requiredRank = rankHandler.getRank(requiredRankName);

            Mine mine = new Mine(mineName, world, bounds, blocks, weights, requiredRank);
            mineList.add(mine);
        }

        plugin.getLogger().info(String.format("Loaded %d mine(s): %d warnings found.", mineList.size(), results.size()));

        // Reset all mines.
        resetAllMines();
        return results;
    }

    public void resetAllMines() {
        for (Mine mine : mineList) {
            mine.resetMine();
        }
    }

    public void resetAllMines(int limit) {
        BukkitRunnable task = new BukkitRunnable() {
            int currentMineIndex = 0;
            int currentMineBlocksPlaced = 0;

            @Override
            public void run() {
                if (currentMineIndex >= mineList.size()) {
                    this.cancel();
                    return;
                }

                Mine currentMine = mineList.get(currentMineIndex);
                List<Block> currentBlocks = currentMine.blocks;

                int blocksPlacedThisTick = 0;

                while (blocksPlacedThisTick < limit) {
                    if (currentMineBlocksPlaced >= currentBlocks.size()) {
                        currentMineIndex += 1;
                        currentMineBlocksPlaced = 0;

                        if (currentMineIndex >= mineList.size()) {
                            this.cancel();
                            return;
                        }

                        currentMine = mineList.get(currentMineIndex);
                        currentBlocks = currentMine.blocks;
                    }

                    Block block = currentBlocks.get(currentMineBlocksPlaced);
                    block.setType(currentMine.getRandomMaterial());
                    currentMineBlocksPlaced += 1;
                    blocksPlacedThisTick += 1;
                }
            }
        };

        task.runTaskTimer(plugin, 1, 1);
    }

    public void clearMines() {
        mineList.clear();
    }

    public Mine getMineFromBlock(Block block) {
        for (Mine mine : mineList) {
            if (mine.containsBlock(block)) return mine;
        }
        return null;
    }
}
