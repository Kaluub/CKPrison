package ca.ckgames.ckprison.commands;

import ca.ckgames.ckprison.Config;
import ca.ckgames.ckprison.Utils;
import ca.ckgames.ckprison.mines.Mine;
import ca.ckgames.ckprison.mines.MineHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class ResetMines implements CommandExecutor {
    MineHandler mineHandler;
    Config config;

    public ResetMines(MineHandler mineHandler, Config config) {
        this.mineHandler = mineHandler;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (Arrays.stream(args).anyMatch(s -> s.equalsIgnoreCase("-f"))) {
            sender.sendMessage(Utils.format("&7Resetting all mines with &cno delay&7."));
            mineHandler.resetAllMines();
            return true;
        }

        int limit = config.blocksPlacedPerTick;
        if (args.length > 0) {
            try {
                limit = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Utils.format(ChatColor.RED + "Invalid input! Usage: /resetmines [-f/blocks per tick]"));
                return true;
            }
        }

        if (limit <= 0) {
            sender.sendMessage(Utils.format(ChatColor.RED + "Invalid input! Usage: /resetmines [-f/blocks per tick]"));
            return true;
        }

        mineHandler.resetAllMines(limit);
        sender.sendMessage(Utils.format(ChatColor.GREEN + "Resetting all mines."));
        double totalBlocks = 0;
        for (Mine mine : mineHandler.mineList) {
            totalBlocks += mine.blocks.length;
        }
        double timeTakenInTicks = totalBlocks / limit;
        sender.sendMessage(Utils.format(ChatColor.GRAY + "At %d blocks per tick modifying %d blocks, reset will take %.2f ticks (%.2fs).", limit, (int) totalBlocks, timeTakenInTicks, timeTakenInTicks/20));
        return true;
    }
}
