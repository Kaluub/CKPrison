package ca.ckgames.ckprison.commands;

import ca.ckgames.ckprison.mines.MineHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ResetMines implements CommandExecutor {
    MineHandler mineHandler;

    public ResetMines(MineHandler mineHandler) {
        this.mineHandler = mineHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        mineHandler.resetAllMines();
        sender.sendMessage(ChatColor.GREEN + "Reset all of the mines.");
        return true;
    }
}
