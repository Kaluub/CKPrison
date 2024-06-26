package ca.ckgames.ckprison;

import org.bukkit.configuration.file.FileConfiguration;

public class Config {
    public String defaultRank;
    public int resetInterval;
    public int maximumMineSize;
    public boolean automaticProtection;
    public boolean rankBasedAccess;
    public String pickaxeCommand;
    public int blocksPlacedPerTick;

    public void setupConfig(FileConfiguration configFile) {
        defaultRank = configFile.getString("default_rank", "none");
        resetInterval = configFile.getInt("reset_interval", 600);
        maximumMineSize = configFile.getInt("maximum_mine_size", 50);
        automaticProtection = configFile.getBoolean("automatic_protection", false);
        rankBasedAccess = configFile.getBoolean("rank_based_protection", false);
        pickaxeCommand = configFile.getString("pickaxe_command", null);
        blocksPlacedPerTick = configFile.getInt("blocks_placed_per_tick", 1000);
    }
}
