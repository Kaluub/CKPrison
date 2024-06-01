package ca.ckgames.ckprison.commands;

import ca.ckgames.ckprison.Config;
import ca.ckgames.ckprison.Utils;
import ca.ckgames.ckprison.ranks.RankHandler;
import ca.ckgames.ckprison.ranks.RankLoadingResult;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class ReloadRanks implements CommandExecutor {
    Plugin plugin;
    Config config;
    RankHandler rankHandler;

    public ReloadRanks(Plugin plugin, Config config, RankHandler rankHandler) {
        this.plugin = plugin;
        this.config = config;
        this.rankHandler = rankHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.reloadConfig();
        FileConfiguration fileConfig = plugin.getConfig();
        config.setupConfig(fileConfig);
        List<RankLoadingResult> loadingResults = rankHandler.loadRanks(plugin.getConfig());
        List<String> rankLoadingErrors = new ArrayList<>();

        for (RankLoadingResult loadingResult : loadingResults) {
            if (!loadingResult.success()) {
                rankLoadingErrors.add(loadingResult.error());
            }
        }

        if (!rankLoadingErrors.isEmpty()) {
            rankHandler.clearRanks();
            String message = Utils.format(ChatColor.RED + "Ranks could not be loaded! See %s below:", rankLoadingErrors.size() > 1 ? "errors" : "error");
            sender.sendMessage(message);
            for (String error : rankLoadingErrors) {
                sender.sendMessage(ChatColor.RED + error);
            }
        } else {
            sender.sendMessage(ChatColor.GREEN + "Successfully reloaded all ranks from the config file.");
        }

        return true;
    }
}