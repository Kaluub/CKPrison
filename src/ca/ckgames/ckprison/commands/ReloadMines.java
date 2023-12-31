package ca.ckgames.ckprison.commands;

import ca.ckgames.ckprison.Config;
import ca.ckgames.ckprison.mines.MineHandler;
import ca.ckgames.ckprison.mines.MineLoadingResult;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class ReloadMines implements CommandExecutor {
    Plugin plugin;
    Config config;
    MineHandler mineHandler;

    public ReloadMines(Plugin plugin, Config config, MineHandler mineHandler) {
        this.plugin = plugin;
        this.config = config;
        this.mineHandler = mineHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.reloadConfig();
        List<MineLoadingResult> loadingResults = mineHandler.loadMines(plugin, config, plugin.getConfig());
        List<String> mineLoadingErrors = new ArrayList<>();

        for (MineLoadingResult loadingResult : loadingResults) {
            if (!loadingResult.success()) {
                mineLoadingErrors.add(loadingResult.error());
            }
        }

        if (!mineLoadingErrors.isEmpty()) {
            mineHandler.clearMines();
            sender.sendMessage(ChatColor.RED + "Mines could not be loaded! See error(s) below:");
            for (String error : mineLoadingErrors) {
                sender.sendMessage(ChatColor.RED + error);
            }
        } else {
            sender.sendMessage(ChatColor.GREEN + "Successfully reloaded and reset all mines from the config file.");
        }

        return true;
    }
}
