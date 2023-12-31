package ca.ckgames.ckprison.commands;

import ca.ckgames.ckprison.ranks.RankHandler;
import ca.ckgames.ckprison.ranks.RankLoadingResult;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class ReloadRanks implements CommandExecutor {
    Plugin plugin;
    RankHandler rankHandler;

    public ReloadRanks(Plugin plugin, RankHandler rankHandler) {
        this.plugin = plugin;
        this.rankHandler = rankHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.reloadConfig();
        List<RankLoadingResult> loadingResults = rankHandler.loadRanks(plugin.getConfig());
        List<String> rankLoadingErrors = new ArrayList<>();

        for (RankLoadingResult loadingResult : loadingResults) {
            if (!loadingResult.success()) {
                rankLoadingErrors.add(loadingResult.error());
            }
        }

        if (!rankLoadingErrors.isEmpty()) {
            rankHandler.clearRanks();
            sender.sendMessage(ChatColor.RED + "Ranks could not be loaded! See error(s) below:");
            for (String error : rankLoadingErrors) {
                sender.sendMessage(ChatColor.RED + error);
            }
        } else {
            sender.sendMessage(ChatColor.GREEN + "Successfully reloaded all ranks from the config file.");
        }

        return true;
    }
}