package ca.ckgames.ckprison;

import ca.ckgames.ckprison.commands.*;
import ca.ckgames.ckprison.mines.MineHandler;
import ca.ckgames.ckprison.mines.MineLoadingResult;
import ca.ckgames.ckprison.ranks.RankHandler;
import ca.ckgames.ckprison.ranks.RankLoadingResult;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CKPrisonPlugin extends JavaPlugin {
    public Config config;
    public SQLiteHandler databaseHandler;
    public RankHandler rankHandler;
    public MineHandler mineHandler;
    public Economy economy;

    @Override
    public void onEnable() {
        // Setup config...
        saveDefaultConfig();
        FileConfiguration configFile = getConfig();
        config = new Config();
        config.setupConfig(configFile);

        // Setup SQLite...
        databaseHandler = new SQLiteHandler(this);
        databaseHandler.setupTables(config.defaultRank);

        // Setup ranks...
        rankHandler = new RankHandler(this);
        List<RankLoadingResult> rankLoadingResults = rankHandler.loadRanks(configFile);
        List<String> rankLoadingErrors = new ArrayList<>();

        for (RankLoadingResult loadingResult : rankLoadingResults) {
            if (!loadingResult.success()) {
                rankLoadingErrors.add(loadingResult.error());
            }
        }

        if (!rankLoadingErrors.isEmpty()) {
            rankHandler.clearRanks();
            getLogger().severe("Ranks could not be loaded! See error(s) below:");
            for (String error : rankLoadingErrors) {
                getLogger().severe(error);
            }
        }

        // Setup mines...
        mineHandler = new MineHandler(rankHandler, this);
        List<MineLoadingResult> mineLoadingResults = mineHandler.loadMines(config, configFile);
        List<String> mineLoadingErrors = new ArrayList<>();

        for (MineLoadingResult loadingResult : mineLoadingResults) {
            if (!loadingResult.success()) {
                mineLoadingErrors.add(loadingResult.error());
            }
        }

        if (!mineLoadingErrors.isEmpty()) {
            mineHandler.clearMines();
            getLogger().severe("Mines could not be loaded! See error(s) below:");
            for (String error : mineLoadingErrors) {
                getLogger().severe(error);
            }
        }

        Runnable resetMinesTask = () -> mineHandler.resetAllMines(config.blocksPlacedPerTick);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, resetMinesTask, config.resetInterval * 20L, config.resetInterval * 20L);

        // Setup economy...
        if (!setupEconomy()) {
            getLogger().warning("Vault & an economy plugin (ie. EssentialsX) needs to be installed for the rank up system to work correctly!");
            economy = null;
        }

        // Setup commands...
        Objects.requireNonNull(getCommand("resetmines")).setExecutor(new ResetMines(mineHandler, config));
        Objects.requireNonNull(getCommand("reloadranks")).setExecutor(new ReloadRanks(this, config, rankHandler));
        Objects.requireNonNull(getCommand("reloadmines")).setExecutor(new ReloadMines(this, config, mineHandler));
        Objects.requireNonNull(getCommand("setrank")).setExecutor(new SetRank(databaseHandler, rankHandler));
        Objects.requireNonNull(getCommand("rankup")).setExecutor(new Rankup(databaseHandler, rankHandler, economy));

        // Setup events...
        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new EventListener(config, databaseHandler, mineHandler, rankHandler), this);
    }

    boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false; // Can't use Vault API.
        }
        RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);
        if (provider  == null) {
            return false;
        }
        economy = provider.getProvider();
        return true;
    }
}
